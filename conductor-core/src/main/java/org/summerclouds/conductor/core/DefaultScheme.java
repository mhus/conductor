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
package org.summerclouds.conductor.core;

import org.summerclouds.common.core.error.NotFoundException;
import org.summerclouds.common.core.util.MUri;
import org.summerclouds.conductor.api.AScheme;
import org.summerclouds.conductor.api.ConUtil;
import org.summerclouds.conductor.api.Conductor;
import org.summerclouds.conductor.api.Scheme;

import java.io.File;
import java.io.IOException;

@AScheme(name = "default")
public class DefaultScheme implements Scheme {

    @Override
    public File load(Conductor con, MUri uri) throws IOException, NotFoundException {
        String path = uri.getPath();

        Scheme mvnScheme = con.getSchemes().get("mvn");
        MUri defUri = ConUtil.getDefaultConfiguration(path);
        return mvnScheme.load(con, defUri);
    }
}
