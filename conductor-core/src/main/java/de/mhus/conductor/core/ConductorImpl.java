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
package de.mhus.conductor.core;

import de.mhus.commons.tree.IReadProperties;
import de.mhus.commons.tree.MProperties;
import de.mhus.conductor.api.*;

import java.io.File;
import java.util.List;
import java.util.Map;

public class ConductorImpl implements Conductor {

    protected PluginsImpl plugins = new PluginsImpl();
    protected LifecyclesImpl lifecycles = new LifecyclesImpl();
    protected ProjectsImpl projects = new ProjectsImpl();
    protected MProperties properties = new MProperties();
    protected File root;
    protected Schemes schemes = new SchemesImpl();
    public Map<String, Validator> validators;
    private Labels generalSelector;
    private List<String> generalOrderBy;

    public ConductorImpl(File rootDir) {
        this.root = rootDir.getAbsoluteFile();
    }

    @Override
    public Plugins getPlugins() {
        return plugins;
    }

    @Override
    public Lifecycles getLifecycles() {
        return lifecycles;
    }

    @Override
    public Projects getProjects() {
        return projects;
    }

    @Override
    public IReadProperties getProperties() {
        return properties;
    }

    @Override
    public File getRoot() {
        return root;
    }

    @Override
    public Schemes getSchemes() {
        return schemes;
    }

    @Override
    public ClassLoader getClassLoader() {
        return getClass().getClassLoader();
    }

    @Override
    public void close() {}

    public Map<String, Validator> getValidators() {
        return validators;
    }

    @Override
    public boolean isVerboseOutput() {
        return getProperties().getBoolean(ConUtil.PROPERTY_VERBOSE, false);
    }

    @Override
    public Labels getGeneralSelector() {
        return generalSelector;
    }

    @Override
    public List<String> getGeneralOrderBy() {
        return generalOrderBy;
    }

    public void setGeneralSelector(Labels generalSelector) {
        this.generalSelector = generalSelector;
    }

    public void setGeneralOrderBy(List<String> generalOrderBy) {
        this.generalOrderBy = generalOrderBy;
    }

}
