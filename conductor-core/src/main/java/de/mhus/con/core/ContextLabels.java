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

import java.util.Iterator;

import de.mhus.con.api.Labels;

public class ContextLabels implements Labels {

    private ContextImpl context;
    private Labels inst;

    public ContextLabels(ContextImpl context, Labels inst) {
        this.context = context;
        this.inst = inst;
    }

    @Override
    public String[] get(String name) {
        return context.make(inst.get(name));
    }

    @Override
    public String[] keys() {
        return inst.keys();
    }

    @Override
    public String[] getOrNull(String name) {
        return context.make(inst.getOrNull(name));
    }

    @Override
    public int size() {
        return inst.size();
    }

    @Override
    public Iterator<String[]> iterator() {
        return inst.iterator();
    }

    
    @Override
    public boolean matches(Labels selector) {
        for (String sKey : selector.keys()) {
            String sValue = selector.get(sKey)[0];
            String[] lValues = getOrNull(sKey);
            if (lValues == null) {
//                log().t(sKey, "not found in project");
                return false;
            }
            boolean ok = false;
            x: for (String lValue : lValues) {
                if (lValue.equals(sValue) || lValue.matches(sValue)) {
                    ok = true;
                    break x;
                }
            }
            if (!ok) {
//                log().t(lValues, "not matches", sValue);
                return false;
            }
        }
        return true;
    }

//    @Override
//    public boolean matches(Labels selector) {
//        for (String sKey : selector.keys()) {
//            String sValue = selector.get(sKey);
//            String lValue = getOrNull(sKey);
//            if (lValue == null) return false;
//            if (!lValue.matches(sValue)) return false;
//        }
//        return true;
//    }

    @Override
    public String toString() {
        return inst.toString();
    }

    public Labels getInstance() {
        return inst;
    }
}
