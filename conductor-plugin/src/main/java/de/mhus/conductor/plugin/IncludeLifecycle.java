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

 
import de.mhus.conductor.api.Lifecycle;
import de.mhus.conductor.api.*;
import de.mhus.conductor.core.ExecutorImpl;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@AMojo(name = "con.includeLifecycle",target="include")
public class IncludeLifecycle  implements ExecutePlugin {

    @Override
    public boolean execute(Context context) throws Exception {
        for (String arg : context.getStep().getArguments()) {
            LOGGER.debug(">>> Include Lifecycle", arg);
            Lifecycle lifecycle = context.getConductor().getLifecycles().get(arg);
            for (Step step : lifecycle.getSteps()) {
            	((ExecutorImpl)context.getExecutor()).executeInternal(step, context.getProject(), context.getCallLevel()+1);
            }
            LOGGER.debug("<<< End Lifecycle ", arg);
        }
        return true;
    }
}
