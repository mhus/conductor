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
package de.mhus.con.core.test;

import de.mhus.con.api.ConUtil;
import de.mhus.con.api.Conductor;
import de.mhus.con.api.Scheme;
import org.summerclouds.common.core.tool.MFile;
import org.summerclouds.common.core.util.MUri;
import org.summerclouds.common.junit.TestCase;

import java.io.File;
import java.io.IOException;

public class DummyScheme extends TestCase implements Scheme {

    @Override
    public File load(Conductor con, MUri uri) throws IOException {
        File f = ConUtil.createTempFile(con, DummyScheme.class, ".yml");
        MFile.writeFile(f, "");
        return f;
    }
}
