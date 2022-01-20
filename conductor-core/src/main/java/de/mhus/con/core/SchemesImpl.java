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

import de.mhus.con.api.Scheme;
import de.mhus.con.api.Schemes;
import de.mhus.lib.core.util.MUri;

public class SchemesImpl extends XCollection<Scheme> implements Schemes {

    @Override
    public Scheme get(MUri uri) {
        return get(uri.getScheme());
    }
}
