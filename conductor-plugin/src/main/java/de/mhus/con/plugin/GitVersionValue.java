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
package de.mhus.con.plugin;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import de.mhus.con.api.AMojo;
import de.mhus.con.api.ConUtil;
import de.mhus.con.api.Context;
import de.mhus.con.api.MojoException;
import de.mhus.con.api.ValuePlugin;
import de.mhus.lib.core.MLog;
import de.mhus.lib.core.logging.Log;
import de.mhus.lib.errors.NotFoundException;

@AMojo(name = "git.version",target = "git.version")
public class GitVersionValue extends MLog implements ValuePlugin {

    private static String version;
    
    @Override
    public String getValue(Context context, String value, Map<String, Object> attributes) throws Exception {
        initGitVersion(context, log());
        return version;
    }

    private synchronized void initGitVersion(Context context, Log log) throws NotFoundException, IOException, MojoException {
        if (version != null) return;
        String gitPath = ConUtil.cmdLocation(context.getConductor(), "git");
        String cmd = gitPath + " --version";
        String[] res = ConUtil.execute(
                context.getConductor(),
                "maven version", 
                new File("."), 
                cmd, 
                false);
        if (!res[2].equals("0"))
            throw new MojoException(context, "not successful", cmd, res[1], res[2]);

        version = res[0].split(" ")[2];
    }

}
