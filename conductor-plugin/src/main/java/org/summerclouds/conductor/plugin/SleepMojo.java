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

import org.summerclouds.common.core.log.MLog;
import org.summerclouds.common.core.tool.MPeriod;
import org.summerclouds.common.core.tool.MThread;
import org.summerclouds.conductor.api.AMojo;
import org.summerclouds.conductor.api.Context;
import org.summerclouds.conductor.api.ExecutePlugin;
import org.summerclouds.conductor.api.Plugin.SCOPE;

@AMojo(name = "sleep",target = "sleep", scope = SCOPE.STEP)
public class SleepMojo extends MLog implements ExecutePlugin {

    @Override
    public boolean execute(Context context) throws Exception {
        long interval = MPeriod.toMilliseconds(context.getProperties().getString("interval"), 0);
        MThread.sleep(interval);
        return true;
    }

}
