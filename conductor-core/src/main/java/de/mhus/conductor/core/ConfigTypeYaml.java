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

import de.mhus.commons.yaml.MYaml;
import de.mhus.commons.yaml.YMap;
import de.mhus.conductor.api.AConfigType;
import de.mhus.conductor.api.Conductor;
import de.mhus.conductor.api.ConfigType;

@AConfigType(name = {"yml", "yaml"})
public class ConfigTypeYaml implements ConfigType {

    @Override
    public YMap create(Conductor con, String content) {
        return MYaml.loadMapFromString(content);
    }
}
