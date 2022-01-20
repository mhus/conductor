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

import java.util.LinkedList;
import java.util.List;

import de.mhus.con.api.Conductor;
import de.mhus.con.api.Labels;
import de.mhus.con.api.Project;
import de.mhus.con.api.Projects;

public class ProjectsImpl extends XCollection<Project> implements Projects {

    @SuppressWarnings("unused")
    private Conductor con;

    @Override
    public List<Project> select(Labels selector) {
        LinkedList<Project> ret = new LinkedList<>();
        collection.forEach(
                (k, v) -> {
                    if (v.getLabels().matches(selector)) ret.add(v);
                });
        return ret;
    }

    public void init(Conductor con) {
        this.con = con;
    }

    @Override
    public List<Project> getAll() {
        return new LinkedList<Project>(collection.values());
    }
}
