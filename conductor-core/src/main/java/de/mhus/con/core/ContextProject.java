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
import java.util.Map.Entry;

import de.mhus.con.api.ConUtil;
import de.mhus.con.api.Labels;
import de.mhus.con.api.Project;
import de.mhus.lib.core.IReadProperties;
import de.mhus.lib.core.MProperties;

public class ContextProject implements Project {

    private ContextImpl context;
    private Project inst;
    private MProperties properties;
    private File rootDir;
    private ContextLabels labels;

    public ContextProject(ContextImpl context, Project inst) {
        this.context = context;
        this.inst = inst;
    }

    @Override
    public Labels getLabels() {
        if (labels == null) {
            labels = new ContextLabels(context, inst.getLabels());
        }
        return labels;
    }

    @Override
    public String getName() {
        return inst.getName();
    }

    @Override
    public String getPath() {
        return context.make(inst.getPath());
    }

    @Override
    public File getRootDir() {

        if (rootDir == null) rootDir = ConUtil.getFile(context.getConductor().getRoot(), getPath());
        return rootDir;
    }

    @Override
    public IReadProperties getProperties() {
        if (properties == null) {
            properties = new MProperties();
            for (Entry<String, Object> entry : inst.getProperties().entrySet()) {
                Object v = entry.getValue();
                if (v instanceof String) v = context.make((String) v);
                properties.put(entry.getKey(), v);
            }
        }
        return properties;
    }

    @Override
    public String toString() {
        if (inst == null) return "?";
        return inst.toString();
    }

    @Override
    public STATUS getStatus() {
        return inst.getStatus();
    }

    public Project getInstance() {
        return inst;
    }
}
