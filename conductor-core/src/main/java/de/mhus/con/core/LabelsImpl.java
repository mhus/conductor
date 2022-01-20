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

import de.mhus.con.api.Labels;

public class LabelsImpl extends XCollection<String[]> implements Labels {

    public void put(String name, String entry) {
        collection.put(name, new String[] {entry});
    }
    
    @Override
    public boolean matches(Labels selector) {
        for (String sKey : selector.keys()) {
            String sValue = selector.get(sKey)[0];
            String[] lValues = getOrNull(sKey);
            if (lValues == null) {
                log().t(sKey, "not found in project");
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
                log().t(lValues, "not matches", sValue);
                return false;
            }
        }
        return true;
    }
}
