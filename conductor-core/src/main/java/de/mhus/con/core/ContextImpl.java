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

import java.util.List;
import java.util.Map;

import de.mhus.con.api.Conductor;
import de.mhus.con.api.Context;
import de.mhus.con.api.Executor;
import de.mhus.con.api.Plugin;
import de.mhus.con.api.Project;
import de.mhus.con.api.Step;
import de.mhus.lib.core.IReadProperties;
import de.mhus.lib.core.MLog;
import de.mhus.lib.core.MProperties;
import de.mhus.lib.core.MSystem;
import de.mhus.lib.core.parser.StringCompiler;
import de.mhus.lib.errors.MException;
import de.mhus.lib.errors.MRuntimeException;

public class ContextImpl extends MLog implements Context {

    private MProperties properties;
    private Plugin plugin;
    private Project project;
    private Conductor con;
    private Step step;
    private Executor executor;
    private List<Project> projects;
    private StringCompiler compiler = new ContextCompiler(this);
    private int callLevel = 0;
    
    public ContextImpl(Conductor con, int callLevel) {
        this.con = con;
        this.callLevel = callLevel;
        properties = new MProperties();
        putReadProperties("", con.getProperties());
    }

    public ContextImpl(Conductor con, IReadProperties additional) {
        this.con = con;
        properties = new MProperties();
        putReadProperties("", con.getProperties());
        putReadProperties("", additional);
    }

    public void putReadProperties(String prefix, IReadProperties m) {
        if (m == null) return;
        for (Map.Entry<? extends String, ? extends Object> e : m.entrySet())
            properties.put(prefix + e.getKey(), e.getValue());
    }

    @Override
    public String make(String in) {
        if (in == null) return null;
        try {
            String ret = compiler.compileString(in).execute(properties);
            log().t("make", in, ret);
            return ret;
        } catch (MException e) {
            log().t(in, e);
            throw new MRuntimeException(in, e);
        }
    }
    
    public String[] make(String[] in) {
        if (in == null) return null;
        String[] out = new String[in.length];
        for (int i = 0; i < in.length; i++)
            out[i] = make(in[i]);
        return out;
    }
    

    public void init(
            Executor executor, List<Project> projects, Project project, Plugin plugin, Step step) {
        this.project = project == null ? null : new ContextProject(this, project);
        this.plugin = new ContextPlugin(this, plugin);
        this.step = new ContextStep(this, step);
        this.executor = executor;
        this.projects = projects;

        if (step != null) {
            putReadProperties("step.", step.getProperties());
            properties.put("step._title", step.getTitle());
            properties.put("step._target", step.getTarget());
        }
        if (project != null) {
            putReadProperties("project.", project.getProperties());
            properties.put("project._name", project.getName());
        }
        if (con != null && con.getProjects() != null)
            for (Project p : con.getProjects())
                putReadProperties("projects." + p.getName() + ".", p.getProperties());

        putReadProperties("", con.getProperties());

        log().t("init", project, plugin, step, properties);
    }

    @Override
    public Conductor getConductor() {
        return con;
    }

    @Override
    public Plugin getPlugin() {
        return plugin;
    }

    @Override
    public Project getProject() {
        return project;
    }

    @Override
    public Step getStep() {
        return step;
    }

    @Override
    public IReadProperties getProperties() {
        return properties;
    }

    @Override
    public String toString() {
        return MSystem.toString(this, plugin, step, project);
    }

    @Override
    public Executor getExecutor() {
        return executor;
    }

    @Override
    public List<Project> getProjects() {
        return projects;
    }

    @Override
    public String getRecursiveProperty(String key, String def) {
        String value = step.getProperties().getString(key, null);
        if (value == null && project != null) value = project.getProperties().getString(key, null);
        if (value == null && con != null) {
            value = con.getProperties().getString(key, null);
            if (value != null) value = make(value);
        }
        if (value == null) value = def;
        return value;
    }

    @Override
    public int getCallLevel() {
        return callLevel;
    }
}
