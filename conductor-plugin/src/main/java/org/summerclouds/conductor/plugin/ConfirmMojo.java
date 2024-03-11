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
package de.mhus.conductor.plugin;

import de.mhus.common.core.console.Console;
import de.mhus.common.core.tool.MString;
import de.mhus.conductor.api.*;
import de.mhus.conductor.api.Plugin.SCOPE;
import de.mhus.conductor.core.ContextStep;
import de.mhus.conductor.core.ExecutorImpl;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Scanner;

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

        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        while (true) {

            console.setColor(Console.COLOR.RED, null);
            console.print(prompt);
            console.cleanup();

            console.print(" (y/n) ");
            console.flush();

            String line = br.readLine().trim();
            if (line.equals("n")) {
                console.println();
                if (mode.equals("exit"))
                    throw new StopLifecycleException(context, "Not Confirmed",prompt);
                return false;
            }
            if (line.equals("y")) {
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
