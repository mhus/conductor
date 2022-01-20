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

import java.io.File;

import de.mhus.con.api.AValidator;
import de.mhus.con.api.Conductor;
import de.mhus.con.api.Context;
import de.mhus.con.api.Project;
import de.mhus.con.api.Validator;
import de.mhus.lib.errors.MException;

@AValidator(name = "maven")
public class MavenValidator implements Validator {

    @Override
    public void validate(Conductor con) throws MException {
        for (Project p : con.getProjects()) {
            // check for root dir
            validate(p);
        }
    }

    @Override
    public void validate(Context context) throws MException {
        validate(context.getProject());
    }

    private void validate(Project p) throws MException {
        File rootDir = p.getRootDir();
        if (!rootDir.exists() || !rootDir.isDirectory())
            throw new MException("project root dir not exists", p, rootDir);
        // check for pom
        File f = new File(rootDir, "pom.xml");
        if (!f.exists() || !f.isFile()) throw new MException("project pom file not exists", p, f);
    }
}
