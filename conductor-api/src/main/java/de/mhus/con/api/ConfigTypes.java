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

import org.summerclouds.common.core.tool.MString;

public interface ConfigTypes extends ICollection<ConfigType> {

    //    default ConfigType get(MUri uri) {
    //        String path = uri.getPath();
    //        String ext = MString.afterLastIndex(path, '.').toLowerCase();
    //        return get(ext);
    //    }

    default ConfigType getForPath(String path) {
        String ext = MString.afterLastIndex(path, '.').toLowerCase();
        return get(ext);
    }
}
