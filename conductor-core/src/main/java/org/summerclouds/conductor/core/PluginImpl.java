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
package org.summerclouds.conductor.core;

import org.summerclouds.common.core.tool.MSystem;
import org.summerclouds.conductor.api.Conductor;
import org.summerclouds.conductor.api.Plugin;

public class PluginImpl implements Plugin {

    protected String target;
    protected String uri;
    protected String mojo;
    protected Conductor con;
    protected SCOPE scope = SCOPE.PROJECT;

    @Override
    public String getTarget() {
        return target;
    }

    @Override
    public String getUri() {
        return uri;
    }

    @Override
    public String getMojo() {
        return mojo;
    }

    public void init(Conductor con) {
        this.con = con;
    }

    @Override
    public SCOPE getScope() {
        return scope;
    }

    @Override
    public String toString() {
        return MSystem.toString(this, target);
    }
}
