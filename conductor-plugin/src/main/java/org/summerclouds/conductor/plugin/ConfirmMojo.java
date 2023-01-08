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
 *#title: Confirm Execution

The plugin will ask the executor to insert y or n. The option y will execute more steps. The other one not.

Modes:



 */
package org.summerclouds.conductor.plugin;

import org.summerclouds.common.core.console.Console;
import org.summerclouds.common.core.tool.MString;
import org.summerclouds.conductor.api.*;
import org.summerclouds.conductor.api.Plugin.SCOPE;
import org.summerclouds.conductor.core.ContextStep;
import org.summerclouds.conductor.core.ExecutorImpl;

@AMojo(name = "confirm",target = "confirm", scope = SCOPE.STEP)
public class ConfirmMojo implements ExecutePlugin {

    @Override
    public boolean execute(Context context) throws Exception {
        if (context.getProperties().getBoolean(ConUtil.PROPERTY_Y, false)) return true;

        Console console = ConUtil.getConsole();

        String mode = context.getStep().getProperties().getString("mode", "exit").toLowerCase().trim();
        String msg = context.getStep().getProperties().getString("message", "");
        if (MString.isSet(msg)) {
            console.println();
            console.println(msg);
        }
        String prompt = context.getStep().getProperties().getString("prompt", "");

        console.setColor(Console.COLOR.RED, null);
        console.print(prompt);
        console.cleanup();
        
        console.print(" (y/n) ");
        console.flush();
        while (true) {
            int input = System.in.read();
            console.print((char)input);
            if (input == 'n') {
                console.println();
                if (mode.equals("exit"))
                    throw new StopLifecycleException(context, "Not Confirmed",prompt);
                return false;
            }
            if (input == 'y') {
                console.println();
                if (mode.equals("execute")) {
                    for (Step caze : context.getStep().getSubSteps()) {
                        ((ExecutorImpl)context.getExecutor()).executeInternalStep( ((ContextStep)caze).getInstance(), context.getProjects(), context.getCallLevel() );
                    }
                }
                return true;
            }
        }
    }

}
