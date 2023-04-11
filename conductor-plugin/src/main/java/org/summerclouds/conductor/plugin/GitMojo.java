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
package org.summerclouds.conductor.plugin;

import org.summerclouds.common.core.M;
import org.summerclouds.common.core.log.MLog;
import org.summerclouds.conductor.api.*;

import java.io.File;

@AMojo(name = "git")
public class GitMojo extends MLog implements ExecutePlugin {

    @Override
    public boolean execute(Context context) throws Exception {
        File dir = context.getProject().getRootDir();
        String gitPath = ConUtil.cmdLocation(context.getConductor(), "git");
        String args = ConUtil.escapeArgumentsForShell(context, context.getStep().getArguments());
        String cmd = gitPath + " " + args;
        String[] res =
                ConUtil.execute(
                        context.getConductor(),
                        context.getStep().getTitle() + " " + context.getProject().getName(),
                        dir,
                        cmd,
                        true);
        if (!res[2].equals("0")
                && !M.to(context.make(context.getProperties()
                        .getString(ConUtil.PROPERTY_STEP_IGNORE_RETURN_CODE, "false")), false))
            throw new MojoException(context, "not successful", cmd, res[1], res[2]);

        return true;
    }
}
