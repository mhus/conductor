/**
 * Copyright (C) 2020 Mike Hummel (mh@mhus.de)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.mhus.con.core;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import de.mhus.con.api.AMojo;
import de.mhus.con.api.ConUtil;
import de.mhus.con.api.Conductor;
import de.mhus.con.api.ConductorPlugin;
import de.mhus.con.api.DirectLoadScheme;
import de.mhus.con.api.ErrorInfo;
import de.mhus.con.api.ExecutePlugin;
import de.mhus.con.api.ExecutionInterceptorPlugin;
import de.mhus.con.api.Executor;
import de.mhus.con.api.Labels;
import de.mhus.con.api.Lifecycle;
import de.mhus.con.api.Plugin;
import de.mhus.con.api.Plugin.SCOPE;
import de.mhus.con.api.Project;
import de.mhus.con.api.Project.STATUS;
import de.mhus.con.api.Scheme;
import de.mhus.con.api.Step;
import de.mhus.con.api.Steps;
import de.mhus.con.api.StopLifecycleException;
import de.mhus.lib.core.MFile;
import de.mhus.lib.core.MLog;
import de.mhus.lib.core.MString;
import de.mhus.lib.core.util.MUri;
import de.mhus.lib.core.util.Value;
import de.mhus.lib.errors.MRuntimeException;
import de.mhus.lib.errors.NotFoundException;

public class ExecutorImpl extends MLog implements Executor {

    private Conductor con;
    private LinkedList<ErrorInfo> errors = new LinkedList<>();
    private Map<String, ConductorPlugin> mojos = new HashMap<>();
    private Map<String, Object[]> pluginClassLoaders = new HashMap<>();
    private LinkedList<ExecutionInterceptorPlugin> interceptors = new LinkedList<>();
    private Lifecycle currentLifecycle;
    private int currentStepCount;

    public ExecutorImpl() {
        interceptors.add(new ExecutionInterceptorDefault()); // TODO dynamic
    }

    @Override
    public void execute(Conductor con, String lifecycle) {
        this.con = con;
        ((ConductorImpl) con).properties.put(ConUtil.PROPERTY_LIFECYCLE, lifecycle);

        currentLifecycle = con.getLifecycles().get(lifecycle);
        checkUsage();
        try {
            log().d("executeLifecycle", currentLifecycle);
            Steps steps = currentLifecycle.getSteps();
            execute(lifecycle, steps);
        } catch (Throwable t) {
            printError(t,currentLifecycle);
            throw new MRuntimeException(currentLifecycle, t);
        } finally {
            errors.clear();
        }
    }

    private void printError(Throwable t, Object ... list ) {
        log().e("===================================================");
        log().e("*** Error: " + t);
        log().e(list);
        while (t != null) {
            t = t.getCause();
            log().e("--- Cause: " + t);
        }
        if (con.isVerboseOutput())
            log().e(t);
    }

    public void checkUsage() {
        boolean error = false;
        for (String arg : currentLifecycle.getUsage()) {
            String[] parts = arg.split(" ",3);
            log().d("validate",arg,parts);
            if (parts.length < 3)
                throw new MRuntimeException(currentLifecycle, "Usage rule malformed",arg);
            String name = parts[0];
            String check = parts[1];
            String msg = parts[2];
            String value = con.getProperties().getString(name, null);
            
            if (check.equals("isset")) {
                if (!MString.isSet(value)) {
                    error = true;
                    log().e(name, check, msg);
                }
            } else
            if (check.equals("notnull")) {
                if (value != null) {
                    error = true;
                    log().e(name, check, msg);
                }
            } else
            if (check.equals("isint")) {
                try {
                    Integer.valueOf(value);
                } catch (NumberFormatException e) {
                    error = true;
                    log().e(name, check, msg);
                }
            } else
            if (check.equals("couldset")) {
                if (!MString.isSet(value)) {
                    log().w(name, check, msg);
                }
            }
        }
        
        if (error)
            throw new MRuntimeException(currentLifecycle, "usage checks failed");
    }

    public void execute(String lifecycle, Steps steps) {
        con.getProjects().forEach(p -> ((ProjectImpl) p).setStatus(Project.STATUS.SKIPPED));
        interceptors.forEach(i -> i.executeBegin(con, lifecycle, steps));
        try {
            currentStepCount = 0;
            for (Step step : steps) execute(step);
        } finally {
            interceptors.forEach(i -> i.executeEnd(con, lifecycle, steps, errors));
        }
    }

    public Closeable enterSubSteps(Step step) {
        interceptors.forEach(i -> i.enterSubSteps(con, step));
        return new Closeable() {
            
            @Override
            public void close() throws IOException {
                ExecutorImpl.this.leaveSubSteps(step);
            }
        };
    }
    
    public void leaveSubSteps(Step step) {
        interceptors.forEach(i -> i.leaveSubSteps(con, step));
    }
    
    public boolean executeInternalStep(Step step, List<Project> projects, int callLevel) {
        if (con != null && con.getProperties().getBoolean(ConUtil.PROPERTY_CONFIRM_STEPS, false)) {
            if (!ConUtil.confirmAction(con, step, projects, "Execute Sub-Step " + step))
                return false;
        }
        log().d("executeInternalStep", step);
        boolean done = false;
        String target = step.getTarget();
        Plugin plugin = con.getPlugins().get(target);
        if (plugin.getScope() == SCOPE.STEP) {
            done = executeInternal(step, null, callLevel);
        } else {
            for (Project project : projects) {
                if (executeInternal(step, project, callLevel))
                    done = true;
            }
        }
        return done;
    }
    
    public boolean executeInternal(Step step, Project project, int callLevel) {
        log().d("executeInternal", step, project);
        try {
            // load plugin
            String target = step.getTarget();
            Plugin plugin = con.getPlugins().get(target);

            // unpack project
            if (project != null && project instanceof ContextProject)
                project = ((ContextProject)project).getInstance();
            
            return execute(step, project, plugin, null, callLevel);
        } catch (StopLifecycleException t) {
            throw t;
        } catch (Throwable t) {
            throw new MRuntimeException(step, t);
        }
    }
    
    public void execute(Step step) {
        currentStepCount++;
        log().d("executeStep", step);

        try {
            // load plugin
            String target = step.getTarget();
            Plugin plugin = con.getPlugins().get(target);

            // check for scope

            if (plugin.getScope() == SCOPE.STEP) {
                // scope: step
                if (con != null && con.getProperties().getBoolean(ConUtil.PROPERTY_CONFIRM_STEPS, false)) {
                    if (!ConUtil.confirmAction(con, step, null, "Execute Step " + step))
                        return;
                }
                execute(step, (Project) null, plugin, null, 0);
                return;
            }

            // scope: project

            // select projects
            Labels selector = step.getSelector();
            LinkedList<Project> projects = null;
            if (selector != null) {
                projects = new LinkedList<>(con.getProjects().select(selector));
            } else {
                projects = new LinkedList<>(con.getProjects().getAll());
            }

            // order
            String order = step.getSortBy();
            if (MString.isSet(order)) {
                ConUtil.orderProjects(projects, order, step.isOrderAsc());
            }

            execute(step, projects, plugin);
        } catch (StopLifecycleException t) {
            throw t;
        } catch (Throwable t) {
            throw new MRuntimeException(step, t);
        }
    }

    protected void execute(Step step, LinkedList<Project> projects, Plugin plugin) {
        if (projects == null || projects.size() == 0) log().w("no projects selected", step);

        if (con != null && con.getProperties().getBoolean(ConUtil.PROPERTY_CONFIRM_STEPS, false)) {
            if (!ConUtil.confirmAction(con, step, projects, "Execute Step " + step + " on " + projects ))
                return;
        }
        
        if (con.getProperties().getBoolean(ConUtil.PROPERTY_PARALLEL, false)
                && step.getProperties().getInt(ConUtil.PROPERTY_THREADS, 0) > 0) {
            LinkedList<Project> queue = new LinkedList<>(projects);
            Thread[] threads = new Thread[step.getProperties().getInt(ConUtil.PROPERTY_THREADS, 0)];
            log().d("Parallel", threads.length);
            for (int i = 0; i < threads.length; i++) {
                threads[i] =
                        new Thread(
                                new Runnable() {

                                    @Override
                                    public void run() {
                                        log().d(Thread.currentThread().getId(), "Started");
                                        while (queue.size() > 0) {
                                            Project task = null;
                                            synchronized (queue) {
                                                try {
                                                    task = queue.getFirst();
                                                } catch (NoSuchElementException e) {
                                                    break;
                                                }
                                            }
                                            if (task == null) break; // paranoia
                                            log().d(Thread.currentThread().getId(), "Task", task);
                                            execute(step, task, plugin, projects, 0);
                                        }
                                        log().d(Thread.currentThread().getId(), "Finished");
                                    }
                                });
                threads[i].start();
            }

            boolean done = false;
            int cnt = 0;
            while (!done) {
                done = true;
                for (Thread thread : threads) {
                    if (thread.isAlive()) {
                        done = false;
                    }
                    if (!done)
                        try {
                            Thread.sleep(1000);
                            cnt++;
                            if (cnt % 60 == 0)
                                synchronized (ConUtil.consoleLock) {
                                    System.out.println("Wait for tasks to finish");
                                }
                        } catch (InterruptedException e) {
                            new MRuntimeException(e);
                        }
                }
            }

        } else {
            for (Project project : projects) execute(step, project, plugin, projects, 0);
        }
    }

    protected boolean execute(
            Step step, Project project, Plugin plugin, LinkedList<Project> projectList, int callLevel) {
        log().d(">>>", step.getTitle(), project == null ? "-none-" : project.getName());
        log().t("execute", step, project, plugin);
        try {
            ContextImpl context = new ContextImpl(con, callLevel);

            context.init(this, projectList, project, plugin, step);

            if (!step.matchCondition(context)) {
                log().d("condition not successful", step);
                return false;
            }
            interceptors.forEach(i -> i.executeBegin(context));

            boolean done = false;
            ConductorPlugin impl = loadMojo(context);
            try {
                done = ((ExecutePlugin) impl).execute(context);
            } catch (Throwable t) {
                if (project != null) ((ProjectImpl) project).setStatus(Project.STATUS.FAILURE);
                interceptors.forEach(i -> i.executeError(context, t));
                errors.add(new ErrorsInfoImpl(context, t));
                if (!(t instanceof StopLifecycleException) && con.getProperties().getBoolean(ConUtil.PROPERTY_FAE, false)) {
                    log().e(context, t);
                    return false;
                } else throw t;
            }
            if (project != null && project.getStatus() != STATUS.FAILURE && done)
                ((ProjectImpl) project).setStatus(Project.STATUS.SUCCESS);

            final boolean d = done;
            interceptors.forEach(i -> i.executeEnd(context, d));
            return true;
        } catch (Throwable t) {
            throw new MRuntimeException(project, t);
        }
    }

    public ConductorPlugin loadMojo(ContextImpl context) {
        ConductorPlugin impl = mojos.get(context.getPlugin().getTarget());
        if (impl != null) return impl;
        try {
            impl = createMojo(con, context.getPlugin());
        } catch (Throwable t) {
            throw new MRuntimeException(context.getPlugin(), t);
        }
        mojos.put(context.getPlugin().getTarget(), impl);
        return impl;
    }

    public ConductorPlugin createMojo(Conductor con, Plugin plugin)
            throws IOException, NotFoundException {
        log().d("createMojo", plugin.getUri(), plugin.getMojo());
        String mojoName = plugin.getMojo();

        Object[] entry = pluginClassLoaders.get(plugin.getUri());

        if (entry == null) {
            MUri uri = MUri.toUri(plugin.getUri());
            Scheme scheme = con.getSchemes().get(uri);

            if (scheme instanceof DirectLoadScheme) {
                return ((DirectLoadScheme) scheme).loadPlugin(uri, mojoName);
            }

            File pFile = scheme.load(con, uri);

            Value<JarEntry> metaInfEntry = new Value<>();
            ArrayList<String> classes = new ArrayList<>();
            LinkedList<URL> urls = new LinkedList<>();
            urls.add(pFile.toURI().toURL());
            log().d("Add main JAR", pFile);
            try (JarFile jar = new JarFile(pFile)) {
                jar.stream()
                        .forEach(
                                jarEntry -> {
                                    if (jarEntry.getName().endsWith(".class")) {
                                        classes.add(
                                                jarEntry.getName()
                                                        .replaceAll("/", ".")
                                                        .replace(".class", ""));
                                    } else if (jarEntry.getName()
                                            .equals("META-INF/dependencies.txt")) {
                                        metaInfEntry.value = jarEntry;
                                    }
                                });

                // load dependencies information out of META-INF/dependencies.txt
                if (metaInfEntry.value != null) {
                    InputStream is = jar.getInputStream(metaInfEntry.value);
                    for (String line : MFile.readLines(is, true)) {
                        line = line.trim();
                        if (MString.isSet(line) && !line.startsWith("#")) {
                            MUri uriDep = MUri.toUri(plugin.getUri());

                            if (!uri.getScheme().equals("file")
                                    && uri.getScheme().equals(uriDep.getScheme()))
                                throw new MRuntimeException(
                                        "scheme access denied",
                                        uri.getScheme(),
                                        uriDep.getScheme());

                            Scheme schemeDep = con.getSchemes().get(uriDep);
                            File depFile = schemeDep.load(con, uriDep);
                            urls.add(depFile.toURI().toURL());
                            log().d("Add dependency JAR", depFile);
                        }
                    }
                }
            }

            URLClassLoader cl = new URLClassLoader(urls.toArray(new URL[0]), con.getClassLoader());
            entry = new Object[] {classes, cl};
            pluginClassLoaders.put(plugin.getUri(), entry);
        }

        @SuppressWarnings("unchecked")
        ArrayList<String> classes = (ArrayList<String>) entry[0];
        URLClassLoader cl = (URLClassLoader) entry[1];

        // scan classes
        for (String className : classes) {
            try {
                Class<?> clazz = cl.loadClass(className);
                AMojo mojoDef = clazz.getAnnotation(AMojo.class);
                log().t("class", clazz, mojoDef);
                if (mojoDef != null) {
                    if (mojoDef.name().equals(mojoName)) {
                        ConductorPlugin inst =
                                (ConductorPlugin) clazz.getConstructor().newInstance();
                        return inst;
                    }
                }
            } catch (ClassNotFoundException cnfe) {
                log().t(cnfe);
            } catch (InstantiationException
                    | IllegalAccessException
                    | IllegalArgumentException
                    | InvocationTargetException
                    | NoSuchMethodException
                    | SecurityException ie) {
                log().w(className, ie);
            }
        }

        throw new NotFoundException("Plugin not found", plugin, plugin.getUri(), mojoName);
    }

    @Override
    public Lifecycle getLifecycle() {
        return currentLifecycle;
    }

    @Override
    public int getCurrentStepCount() {
        return currentStepCount;
    }

    public void setConductor(Conductor con) {
        this.con = con;
    }
}
