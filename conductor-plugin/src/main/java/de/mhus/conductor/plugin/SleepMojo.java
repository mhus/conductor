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

 
import de.mhus.commons.tools.MPeriod;
import de.mhus.commons.tools.MThread;
import de.mhus.conductor.api.AMojo;
import de.mhus.conductor.api.Context;
import de.mhus.conductor.api.ExecutePlugin;
import de.mhus.conductor.api.Plugin.SCOPE;

@AMojo(name = "sleep",target = "sleep", scope = SCOPE.STEP)
public class SleepMojo  implements ExecutePlugin {

    @Override
    public boolean execute(Context context) throws Exception {
        long interval = MPeriod.toMilliseconds(context.getProperties().getString("interval").get(), 0);
        MThread.sleep(interval);
        return true;
    }

}
