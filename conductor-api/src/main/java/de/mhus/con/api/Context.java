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
package de.mhus.con.api;

import java.util.List;

import de.mhus.lib.core.IReadProperties;
import de.mhus.lib.errors.MException;

public interface Context {

    String make(String in) throws MException;

    IReadProperties getProperties();

    Plugin getPlugin();

    Project getProject();

    Step getStep();

    Conductor getConductor();

    Executor getExecutor();

    /**
     * Return a list of affected projects for the current step. Could be null if step scope is not
     * PROJECTS
     *
     * @return List or null
     */
    List<Project> getProjects();

    /**
     * Return a property value or default. Search from Step to Project to General.
     *
     * @param key
     * @param def
     * @return Value or default
     */
    String getRecursiveProperty(String key, String def);
    
    int getCallLevel();
    
}
