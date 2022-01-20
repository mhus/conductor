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

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import de.mhus.con.api.ConUtil;
import de.mhus.con.api.Conductor;
import de.mhus.con.api.ConfigType;
import de.mhus.con.api.ConfigTypes;
import de.mhus.con.api.Configurator;
import de.mhus.con.api.DirectLoadScheme;
import de.mhus.con.api.Lifecycle;
import de.mhus.con.api.Plugin;
import de.mhus.con.api.Plugin.SCOPE;
import de.mhus.con.api.Project;
import de.mhus.con.api.Scheme;
import de.mhus.con.api.Schemes;
import de.mhus.con.api.Step;
import de.mhus.con.api.Validator;
import de.mhus.conductor.api.meta.Version;
import de.mhus.lib.core.IProperties;
import de.mhus.lib.core.MCast;
import de.mhus.lib.core.MFile;
import de.mhus.lib.core.MLog;
import de.mhus.lib.core.MProperties;
import de.mhus.lib.core.MString;
import de.mhus.lib.core.MValidator;
import de.mhus.lib.core.util.MUri;
import de.mhus.lib.core.yaml.YList;
import de.mhus.lib.core.yaml.YMap;
import de.mhus.lib.errors.MException;
import de.mhus.lib.errors.MRuntimeException;
import de.mhus.lib.errors.NotFoundException;

public class ConfiguratorImpl extends MLog implements Configurator {

    protected Schemes schemes = new SchemesImpl();
    protected ConfigTypes types = new ConfigTypesImpl();
    protected Conductor con;
    protected Set<String> loadedUris = new HashSet<>();
    protected Map<String, Validator> validators = new HashMap<>();
    protected MProperties defaultProperties = new MProperties();

    @Override
    public void configure(URI uri, Conductor con, IProperties properties) throws MException {
        configure(uri, con, properties, null);
    }
    
    public void configure(URI uri, Conductor con, IProperties properties, LinkedList<String> defaultImport) throws MException {
        this.con = con;
        ((ConductorImpl) con).properties.putReadProperties(defaultProperties);
        ((ConductorImpl) con).schemes = schemes;
        ((ConductorImpl) con).validators = validators;
        if (defaultImport != null) {
            for (String uriStr : defaultImport) {
                loadImport(uriStr);
            }
        }

        overwrite(MUri.toUri(uri.toString()), true);
        initEntries();
        if (properties != null) ((ConductorImpl) con).properties.putReadProperties(properties);
        ((ConductorImpl) con).properties.put(ConUtil.PROPERTY_VERSION, Version.VERSION);
        ((ConductorImpl) con)
                .properties.put(ConUtil.PROPERTY_ROOT, con.getRoot().getAbsolutePath());
        ((ConductorImpl) con)
                .properties.put(ConUtil.PROPERTY_HOME, ConUtil.getHome().getAbsolutePath());

        validate();
    }

    protected void initEntries() {

        ((ProjectsImpl) con.getProjects()).init(con);
        for (Project entry : con.getProjects()) ((ProjectImpl) entry).init(con);

        ((PluginsImpl) con.getPlugins()).init(con);
        for (Plugin entry : con.getPlugins()) ((PluginImpl) entry).init(con);

        for (Lifecycle lifecycle : con.getLifecycles()) {
            ((LifecycleImpl) lifecycle).init(con);
            int cnt = 0;
            for (Step entry : lifecycle.getSteps()) {
                ((StepImpl) entry)
                        .init(con, cnt, lifecycle.getName() + ":" + cnt + ":" + entry.getTarget());
                cnt++;
            }
        }
    }

    protected void validate() throws MException {
        String[] validatorList =
                con.getProperties().getString(ConUtil.PROPERTY_VALIDATORS, "").split(",");
        for (String name : validatorList) {
            if (MString.isEmpty(name)) continue;
            Validator validator = validators.get(name);
            if (validator != null) {
                validator.validate(con);
            } else log().w("Validator not found", name);
        }
    }

    protected void overwrite(MUri uri, boolean rebase) throws MException {
        log().d("load uri", uri);
        loadedUris.add(uri.toString());
        // 1 load resource
        Scheme scheme = schemes.get(uri);
        String content = null;
        String path = null;
        if (scheme instanceof DirectLoadScheme) {
            content = ((DirectLoadScheme) scheme).loadContent(uri);
            path = uri.getPath();
        } else {
            File cf = null;
            try {
                cf = scheme.load(con, uri);
                if (cf != null) content = MFile.readFile(cf);
            } catch (IOException e) {
                throw new MException(e);
            }
            if (cf != null) path = cf.getPath();
        }
        if (content == null) throw new NotFoundException("configuration not found", uri);
        ConfigType type = types.getForPath(path);
        YMap docE = type.create(con, content);
        if (docE.isEmpty()) {
            log().w("Content is empty", uri);
            return;
        }
        YList importE = docE.getList("import");

        // load imports FIRST and fill before processing
        loadImports(importE);

        YMap propertiesE = docE.getMap("properties");
        loadProperties(propertiesE);

        YList pluginsE = docE.getList("plugins");
        loadPlugins(pluginsE);

        YList projectsE = docE.getList("projects");
        loadProjects(projectsE);

        YList lifecyclesE = docE.getList("lifecycles");
        loadLifecycles(lifecyclesE);

        if (rebase) {
            YMap rebaseE = docE.getMap("rebase");
            rebase(rebaseE);
        }
    }

    private void rebase(YMap rebaseE) throws MException {
        if (rebaseE == null) return;

        String base = rebaseE.getString("base", "");
        if (MString.isSet(base)) {

            File baseDir = new File(base);
            if (!baseDir.isAbsolute()) baseDir = new File(con.getRoot(), base).getAbsoluteFile();
            ((ConductorImpl) con).root = baseDir;

            String configFile = rebaseE.getString("configuration", null);
            if (configFile == null) configFile = MainCli.findDefaultFile(con.getRoot());

            // import rebase
            MUri uri = MUri.toUri(configFile);
            log().i("rebase", uri);
            overwrite(uri, false);
        }

        YList projectsE = rebaseE.getList("projects");
        if (projectsE != null) {
            // remove all projects not in list
            HashSet<String> names = new HashSet<>();
            con.getProjects().forEach(p -> names.add(p.getName()));
            projectsE.toStringList().forEach(n -> names.remove(n));
            names.forEach(n -> ((ConductorImpl) con).projects.remove(n));
        }
    }

    private void loadProperties(YMap propertiesE) {
        if (propertiesE == null) return;

        if (propertiesE.getBoolean("_clear")) ((MProperties) con.getProperties()).clear();

        for (String key : propertiesE.getKeys()) {
            if (key.equals("_clear")) continue;
            ((MProperties) con.getProperties()).put(key, propertiesE.getString(key));
        }
    }

    private void loadProjectProperties(YMap propertiesE, ProjectImpl project) {

        ((MProperties) project.getProperties()).put("_name", project.getName());

        if (propertiesE == null) return;

        if (propertiesE.getBoolean("_clear")) ((MProperties) project.getProperties()).clear();

        for (String key : propertiesE.getKeys()) {
            if (key.equals("_clear")) continue;
            ((MProperties) project.getProperties()).put(key, propertiesE.getString(key));
        }
    }

    protected void loadLifecycles(YList lifecyclesE) {
        if (lifecyclesE == null) return;
        for (YMap map : lifecyclesE.toMapList()) {
            loadLifecycle(map);
        }
    }

    protected void loadLifecycle(YMap map) {
        String name = map.getString("name");
        boolean merge = map.getBoolean("_merge");
        YList stepsE = map.getList("steps");

        StepsImpl steps = new StepsImpl();
        if (merge) {
            LifecycleImpl parent = (LifecycleImpl) con.getLifecycles().getOrNull(name);
            if (parent != null) steps = (StepsImpl) parent.getSteps();
        }

        loadSteps(stepsE, steps);

        LifecycleImpl lf = new LifecycleImpl(name, steps);
        lf.setDescription( map.getString("description") );
        YList usageE = map.getList("usage");
        loadUsage(usageE, lf);
        ((LifecyclesImpl) con.getLifecycles()).put(name, lf);
    }

    protected void loadUsage(YList usageE, LifecycleImpl lf) {
        if (usageE == null) return;
        for (String check : usageE.toStringList()) {
            lf.addUsage(check);
        }
    }

    protected void loadSteps(YList executeE, StepsImpl steps) {
        if (executeE == null) return;
        for (YMap map : executeE.toMapList()) {

            Step step = loadStep(map);

            String mode = map.getString("_insert");
            if (mode == null || mode.equals("append")) steps.add(step);
            else if (mode.equals("first")) steps.list.addFirst(step);
            else if (MValidator.isInteger(mode)) steps.list.add(MCast.toint(mode, 0), step);
        }
    }

    protected Step loadStep(YMap map) {
        StepImpl step = new StepImpl();

        // target:
        step.target = map.getString("target");
        // title
        step.title = map.getString("title", step.target);

        try {
            // parameters:
            step.arguments = new LinkedList<>();
            if (map.isString("arguments")) {
                step.arguments.add(map.getString("arguments"));
            } else if (map.isList("arguments")) {
                YList parametersE = map.getList("arguments");
                if (parametersE != null) {
                    parametersE.toStringList().forEach(v -> step.arguments.add(v));
                }
            }

            // selector:
            YMap selectorE = map.getMap("selector");
            step.selector = new LabelsImpl();
            if (selectorE != null) {
                for (String key : selectorE.getKeys()) {
                    if (selectorE.isList(key))
                        step.selector.put(key, selectorE.getStringArray(key));
                    else
                        step.selector.put(key, selectorE.getString(key));
                }
            }

            // order:
            step.sort = map.getString("sort");
            if (step.sort != null) {
                step.sort = step.sort.trim();
                if (step.sort.toLowerCase().endsWith(" asc")) {
                    step.sort = step.sort.substring(0, step.sort.length() - 4);
                    step.orderAsc = true;
                } else if (step.sort.toLowerCase().endsWith(" desc")) {
                    step.sort = step.sort.substring(0, step.sort.length() - 5);
                    step.orderAsc = false;
                }
            }

            // condition
            step.condition = map.getString("condition");

            // properties
            YMap propertiesE = map.getMap("properties");
            loadStepProperties(propertiesE, step);

            // sub steps
            YList subE = map.getList("steps");
            step.steps = new StepsImpl();
            loadSteps(subE, step.steps);

        } catch (Throwable t) {
            throw new MRuntimeException("step", step, t);
        }

        return step;
    }

    private void loadStepProperties(YMap propertiesE, StepImpl step) {
        if (propertiesE == null) return;

        if (propertiesE.getBoolean("_clear")) ((MProperties) step.getProperties()).clear();

        for (String key : propertiesE.getKeys()) {
            if (key.equals("_clear")) continue;
            ((MProperties) step.getProperties()).put(key, propertiesE.getString(key));
        }
    }

    protected void loadProjects(YList projectsE) {
        if (projectsE == null) return;
        // no project overloading
        ((ProjectsImpl) con.getProjects()).collection.clear();

        for (YMap map : projectsE.toMapList()) {
            loadProject(map);
        }
    }

    protected void loadProject(YMap map) {
        ProjectImpl project = new ProjectImpl();

        Project merge =
                map.getBoolean("_merge")
                        ? con.getProjects().getOrNull(map.getString("target"))
                        : null;
        project.name = map.getString("name");
        project.path = map.getString("path", merge == null ? null : merge.getPath());
        YMap l = map.getMap("labels");
        if (l == null && merge == null) project.labels = new LabelsImpl();
        else if (l == null && merge != null) project.labels = merge.getLabels();
        else {
            project.labels = new LabelsImpl();
            for (String key : l.getKeys()) ((LabelsImpl) project.labels).put(key, l.getStringArray(key));
            ((LabelsImpl) project.labels).put("_name", project.name);
        }
        YMap propertiesE = map.getMap("properties");
        loadProjectProperties(propertiesE, project);

        ((ProjectsImpl) con.getProjects()).put(project.getName(), project);
    }

    protected void loadPlugins(YList pluginsE) {
        if (pluginsE == null) return;
        for (YMap map : pluginsE.toMapList()) {
            loadPlugin(map);
        }
    }

    protected void loadPlugin(YMap map) {

        PluginImpl plugin = new PluginImpl();
        Plugin merge =
                map.getBoolean("_merge")
                        ? con.getPlugins().getOrNull(map.getString("target"))
                        : null;

        plugin.target = map.getString("target");
        plugin.uri = map.getString("uri", merge == null ? null : merge.getUri());
        plugin.mojo = map.getString("mojo", merge == null ? null : merge.getMojo());

        String scope = map.getString("scope");
        if (scope != null) plugin.scope = SCOPE.valueOf(scope.toUpperCase().trim());

        ((PluginsImpl) con.getPlugins()).put(plugin.getTarget(), plugin);
    }

    protected void loadImports(YList importE) throws MException {
        if (importE == null) return;
        for (String uriStr : importE.toStringList()) {
            loadImport(uriStr);
        }
    }

    protected void loadImport(String uriStr) throws MException {
        MUri uri = MUri.toUri(uriStr);
        if (loadedUris.contains(uri.toString())) {
            log().d("Ignore, already loaded", uriStr);
        } else {
            overwrite(uri, false);
        }
    }

    public Schemes getSchemes() {
        return schemes;
    }

    public ConfigTypes getTypes() {
        return types;
    }

    public Conductor getcon() {
        return con;
    }

    public Set<String> getLoadedUris() {
        return loadedUris;
    }

    public Map<String, Validator> getValidators() {
        return validators;
    }

    public MProperties getDefaultProperties() {
        return defaultProperties;
    }
}
