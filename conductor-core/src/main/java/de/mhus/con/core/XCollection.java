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

import java.util.HashMap;
import java.util.Iterator;

import de.mhus.con.api.ICollection;
import de.mhus.lib.core.MLog;
import de.mhus.lib.errors.MRuntimeException;

public class XCollection<T> extends MLog implements ICollection<T> {

    protected HashMap<String, T> collection = new HashMap<>();

    @Override
    public T getOrNull(String name) {
        return collection.get(name);
    }

    @Override
    public T get(String name) {
        T ret = collection.get(name);
        if (ret == null) throw new MRuntimeException(getClass().getSimpleName(), "not found", name);
        return ret;
    }

    @Override
    public String[] keys() {
        return collection.keySet().toArray(new String[0]);
    }

    public void put(String name, T entry) {
        collection.put(name, entry);
    }

    public void remove(String name) {
        collection.remove(name);
    }

    @Override
    public Iterator<T> iterator() {
        return collection.values().iterator();
    }

    @Override
    public String toString() {
        return collection.toString();
    }

    @Override
    public int size() {
        return collection.size();
    }
}
