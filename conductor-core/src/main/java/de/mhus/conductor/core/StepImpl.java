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

import de.mhus.commons.errors.InternalRuntimeException;
import de.mhus.commons.errors.MException;
import de.mhus.commons.matcher.Condition;
import de.mhus.commons.tree.IProperties;
import de.mhus.commons.tree.MProperties;
import de.mhus.commons.tools.MString;
import de.mhus.commons.tools.MSystem;
import de.mhus.conductor.api.*;

import java.util.LinkedList;
import java.util.Map;

public class StepImpl implements Step {

    protected LinkedList<String> arguments;
    protected LabelsImpl selector;
    protected String[] order;
    protected String target;
    protected Conductor con;
    protected String condition;
    protected String ident;
    protected String title;
    protected MProperties properties = new MProperties();
    private int id;
    protected StepsImpl steps;

    @Override
    public LinkedList<String> getArguments() {
        return arguments;
    }

    @Override
    public Labels getSelector() {
        return selector;
    }

    @Override
    public String[] getOrderBy() {
        return order;
    }

    @Override
    public String getTarget() {
        return target;
    }

    public void init(Conductor con, int id, String ident) {
        this.con = con;
        this.ident = ident;
        this.id = id;
    }

    @Override
    public String toString() {
        return MSystem.toString(this, title, ident);
    }

    @Override
    public String getCondition() {
        return condition;
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean matchCondition(Context context) {
        String condStr = getCondition();
        if (MString.isEmptyTrim(condStr)) return true;
        if (condStr.equals("skip")) return false;

        try {
            Condition filter = new Condition(condStr);
            return filter.matches((Map<String, Object>) context.getProperties());
        } catch (MException e) {
            throw new InternalRuntimeException(this, condStr, e);
        }
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public IProperties getProperties() {
        return properties;
    }

    @Override
    public int getId() {
        return id;
    }
    
    @Override
    public Steps getSubSteps() {
        return steps;
    }
}
