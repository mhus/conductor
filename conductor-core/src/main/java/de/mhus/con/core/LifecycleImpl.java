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

import de.mhus.con.api.Conductor;
import de.mhus.con.api.Lifecycle;
import de.mhus.con.api.Steps;
import de.mhus.lib.core.MSystem;

public class LifecycleImpl implements Lifecycle {

    private Steps steps;
    private String name;
    private String description = "";
    private LinkedList<String> usage = null;

    @SuppressWarnings("unused")
    private Conductor con;

    public LifecycleImpl(String name, Steps steps) {
        this.name = name;
        this.steps = steps;
    }

    @Override
    public Steps getSteps() {
        return steps;
    }

    @Override
    public String getName() {
        return name;
    }

    public void init(Conductor con) {
        this.con = con;
    }

    @Override
    public String toString() {
        return MSystem.toString(this, name);
    }

    @Override
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void addUsage(String check) {
        if (usage == null)
            usage = new LinkedList<>();
        usage.add(check);
    }
    
    @Override
    public String[]  getUsage() {
        if (usage == null)
            return new String[0];
        return usage.toArray(new String[usage.size()]);
    }
    
}
