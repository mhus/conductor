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
package de.mhus.conductor.plugin;

 
import de.mhus.conductor.api.*;
import lombok.extern.slf4j.Slf4j;

import java.io.File;

@Slf4j
@AMojo(name = "git.clone")
public class GitCloneMojo  implements ExecutePlugin {

    @Override
    public boolean execute(Context context) throws Exception {

        File dir = context.getProject().getRootDir();
        String gitUrl = context.getProject().getProperties().getString("gitUrl", null);
        String gitBranch = context.getProject().getProperties().getString("gitBranch", null);
        if (gitUrl == null) {
             LOGGER.warn("gitUrl not set, skip"); // or error?
            return false;
        }
        if (dir.exists() && dir.isDirectory()) {
            LOGGER.info("project exists, nothing to do");
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
