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

import de.mhus.common.core.node.IProperties;
import de.mhus.common.core.node.MProperties;
import de.mhus.conductor.api.Context;
import de.mhus.conductor.api.Labels;
import de.mhus.conductor.api.Step;
import de.mhus.conductor.api.Steps;

import java.util.LinkedList;
import java.util.Map.Entry;

public class ContextStep implements Step {

    private ContextImpl context;
    private Step inst;
    private ContextLabels selector;
    LinkedList<String> parameters;
    private MProperties properties;

    public ContextStep(ContextImpl context, Step inst) {
        this.context = context;
        this.inst = inst;
    }

    @Override
    public LinkedList<String> getArguments() {
        if (parameters == null) {
            parameters = new LinkedList<>();
            if (inst.getArguments() != null)
                inst.getArguments().forEach(v -> parameters.add(context.make(v)));
        }
        return parameters;
    }

    @Override
    public Labels getSelector() {
        if (selector == null) selector = new ContextLabels(context, inst.getSelector());
        return selector;
    }

    @Override
    public String[] getOrderBy() {
        return context.make(inst.getOrderBy());
    }

    @Override
    public String getTarget() {
        return inst.getTarget();
    }

    @Override
    public String getCondition() {
        return inst.getCondition();
    }

    @Override
    public boolean matchCondition(Context context) {
        return inst.matchCondition(context);
    }

    @Override
    public String toString() {
        return inst.toString();
    }

    @Override
    public String getTitle() {
        return context.make(inst.getTitle());
    }

    @Override
    public IProperties getProperties() {
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
    public int getId() {
        return inst.getId();
    }

    public Step getInstance() {
        return inst;
    }

    @Override
    public Steps getSubSteps() {
        return new ContextSteps(context, inst.getSubSteps());
    }
}
