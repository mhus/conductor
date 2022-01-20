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

import de.mhus.con.api.Plugin;

public class ContextPlugin implements Plugin {

    private ContextImpl context;
    private Plugin inst;

    public ContextPlugin(ContextImpl context, Plugin inst) {
        this.context = context;
        this.inst = inst;
    }

    @Override
    public String getTarget() {
        return inst.getTarget();
    }

    @Override
    public String getUri() {
        return context.make(inst.getUri());
    }

    @Override
    public String getMojo() {
        return context.make(inst.getMojo());
    }

    @Override
    public SCOPE getScope() {
        return inst.getScope();
    }

    @Override
    public String toString() {
        return inst.toString();
    }

    public Plugin getInstance() {
        return inst;
    }
}
