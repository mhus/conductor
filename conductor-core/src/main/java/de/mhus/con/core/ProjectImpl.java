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

import de.mhus.con.api.ConUtil;
import de.mhus.con.api.Conductor;
import de.mhus.con.api.Labels;
import de.mhus.con.api.Project;
import de.mhus.lib.core.IProperties;
import de.mhus.lib.core.MProperties;
import de.mhus.lib.core.MSystem;

public class ProjectImpl implements Project {

    protected String name;
    protected String path;
    protected Labels labels;
    protected Conductor con;
    protected File rootDir;
    protected MProperties properties = new MProperties();
    private STATUS status;

    @Override
    public Labels getLabels() {
        return labels;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getPath() {
        return path;
    }

    public void init(Conductor con) {
        this.con = con;
        rootDir = ConUtil.getFile(con.getRoot(), getPath());
    }

    @Override
    public File getRootDir() {
        return rootDir;
    }

    @Override
    public IProperties getProperties() {
        return properties;
    }

    @Override
    public String toString() {
        return MSystem.toString(this, name);
    }

    public void setStatus(STATUS status) {
        this.status = status;
    }

    @Override
    public STATUS getStatus() {
        return status;
    }
}
