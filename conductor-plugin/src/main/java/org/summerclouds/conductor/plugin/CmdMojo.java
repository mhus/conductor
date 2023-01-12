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

import org.summerclouds.common.core.tool.MString;
import org.summerclouds.conductor.api.AMojo;
import org.summerclouds.conductor.api.ConUtil;
import org.summerclouds.conductor.api.Context;
import org.summerclouds.conductor.api.MojoException;

import java.io.File;

@AMojo(name = "cmd")
public class CmdMojo extends AbstractMavenExecute {

    @Override
    public boolean execute2(File dir, String moduleName, Context context) throws Exception {
        String cmd =
                MString.join(
                        context.getStep().getArguments(), " "); // TODO add quotes and or escapes
        String[] res = ConUtil.execute(
                context.getConductor(),
                getCmdName(context, moduleName), 
                dir, 
                cmd, 
                true);
        if (!res[2].equals("0")
                && !context.getProperties()
                        .getBoolean(ConUtil.PROPERTY_STEP_IGNORE_RETURN_CODE, false))
            throw new MojoException(context, "not successful", cmd, res[1], res[2]);
        return true;
    }
}