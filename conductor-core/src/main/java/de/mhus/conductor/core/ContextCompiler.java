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

import de.mhus.commons.errors.InternalException;
import de.mhus.commons.errors.MException;
import de.mhus.commons.lang.IValuesProvider;
import de.mhus.commons.parser.StringCompiler;
import de.mhus.commons.parser.StringPart;
import de.mhus.commons.tools.MString;
import de.mhus.conductor.api.Plugin;
import de.mhus.conductor.api.ValuePlugin;

public class ContextCompiler extends StringCompiler {

    private ContextImpl context;

    public ContextCompiler(ContextImpl context) {
        this.context = context;
    }

    @Override
    protected StringPart createAttributePart(String part) {
        if (part.trim().endsWith(")")) {
            part = part.trim();
            int pos = part.indexOf('(');
            if (pos > 0) {
                String name = part.substring(0,pos);
                String value = part.substring(pos+1, part.length()-1);
                return new PluginPart(name,value);
            }
        }
        return super.createAttributePart(part);
    }

    public class PluginPart implements StringPart {

        private String name;
        private String value;

        public PluginPart(String name, String value) {
            this.name = name;
            this.value = value;
        }

        @Override
        public void execute(StringBuilder out, IValuesProvider attributes) throws MException {
            Plugin plugin = context.getConductor().getPlugins().get(name);
            try {
                ValuePlugin mojo = (ValuePlugin) ((ExecutorImpl)context.getExecutor()).createMojo(context.getConductor(), plugin);
                String v = mojo.getValue(context, value, attributes);
                out.append(v);
            } catch (Throwable t) {
                throw new InternalException(name,value,t);
            }
        }

        @Override
        public void dump(int level, StringBuilder out) {
            MString.appendRepeating(level, ' ', out);
            out.append(getClass().getCanonicalName())
                .append(" ")
                .append(name)
                .append("(")
                .append(value)
                .append(")");
        }
    }
}
