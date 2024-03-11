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
/*#man mojo
 *#title: Execute Sub Steps

For each project execute the sub steps in a row.
 
IMPORTANT: You can't use scope:step plugins as sub steps.

 */
package de.mhus.conductor.plugin;

import de.mhus.common.core.log.MLog;
import de.mhus.conductor.api.AMojo;
import de.mhus.conductor.api.Context;
import de.mhus.conductor.api.ExecutePlugin;
import de.mhus.conductor.api.Step;
import de.mhus.conductor.core.ContextStep;
import de.mhus.conductor.core.ExecutorImpl;

import java.io.Closeable;

@AMojo(name = "execute",target = "execute")
public class ExecuteMojo extends MLog implements ExecutePlugin {

    @Override
    public boolean execute(Context context) throws Exception {
        boolean done = false;
        try ( Closeable x = ((ExecutorImpl)context.getExecutor()).enterSubSteps(context.getStep()) ) {
            for (Step caze : context.getStep().getSubSteps()) {
                done = true;
                ((ExecutorImpl)context.getExecutor()).executeInternal( ((ContextStep)caze).getInstance(), context.getProject(), context.getCallLevel()+1 );
            }
        }
        return done;
    }

}
