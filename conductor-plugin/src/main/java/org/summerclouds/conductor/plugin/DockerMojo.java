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

import de.mhus.common.core.log.MLog;
import de.mhus.common.core.tool.MString;
import de.mhus.conductor.api.*;

import java.io.File;

@AMojo(name = "docker")
public class DockerMojo extends MLog implements ExecutePlugin {

    @Override
    public boolean execute(Context context) throws Exception {
        String dockerPath = ConUtil.cmdLocation(context.getConductor(), "docker");
        String namespace = context.getRecursiveProperty("dockerNamespace", null);

        String cmd =
                dockerPath
                        + " "
                        + (namespace == null ? "" : "-ns " + namespace + " ")
                        + MString.join(context.getStep().getArguments(), " ");

        String path = context.getProject().getProperties().getString("dockerPath", null);
        if (path == null) {
            log().d("dockerPath not found in project");
            return false;
        }

        File dir = new File(context.getProject().getRootDir(), path);
        if (!dir.exists() || !dir.isDirectory()) {
            log().d("dockerPath not exists", path, dir.getAbsolutePath());
            return false;
        }

        String[] res =
                ConUtil.execute(
                        context.getConductor(),
                        "docker " + context.getProject().getName() + "/" + path, 
                        dir, 
                        cmd, 
                        true);
        if (!res[2].equals("0"))
            throw new MojoException(context, "not successful", cmd, res[1], res[2]);
        return true;
    }
}
