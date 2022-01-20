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

import de.mhus.con.api.AMojo;
import de.mhus.con.api.ConUtil;
import de.mhus.con.api.Context;
import de.mhus.con.api.ExecutePlugin;
import de.mhus.con.api.MojoException;
import de.mhus.lib.core.MLog;

@AMojo(name = "git.clone")
public class GitCloneMojo extends MLog implements ExecutePlugin {

    @Override
    public boolean execute(Context context) throws Exception {

        File dir = context.getProject().getRootDir();
        String gitUrl = context.getProject().getProperties().getString("gitUrl", null);
        String gitBranch = context.getProject().getProperties().getString("gitBranch", null);
        if (gitUrl == null) {
            log().w("gitUrl not set, skip"); // or error?
            return false;
        }
        if (dir.exists() && dir.isDirectory()) {
            log().i("project exists, nothing to do");
            return false;
        }

        // create directory
        dir.mkdirs();

        String gitPath = ConUtil.cmdLocation(context.getConductor(), "git");
        String cmd =
                gitPath
                        + " clone --progress "
                        + gitUrl
                        + " ."
                        + (gitBranch != null ? " -b " + gitBranch : "");
        String[] res =
                ConUtil.execute(
                        context.getConductor(),
                        context.getStep().getTitle() + " " + context.getProject().getName(),
                        dir,
                        cmd,
                        true);
        if (!res[2].equals("0"))
            throw new MojoException(context, "not successful", cmd, res[1], res[2]);
        return true;
    }
}
