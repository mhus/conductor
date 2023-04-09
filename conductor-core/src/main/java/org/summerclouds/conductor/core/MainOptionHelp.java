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
package org.summerclouds.conductor.core;

import org.summerclouds.conductor.api.AOption;
import org.summerclouds.conductor.api.Cli;
import org.summerclouds.conductor.api.MainOptionHandler;

import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.TreeMap;

@AOption(alias = {"-help","-h"})
public class MainOptionHelp implements MainOptionHandler {

    @Override
    public void execute(Cli cli, String cmd, LinkedList<String> queue) {
        System.out.println("""
                Usage:
                  con [options/properties] <lifecycle name> [arguments]
                  options: Prepare the execution of the lifecycle or options of the engine. Starting with '-'.
                  lifecycle name: Name of the lifecycle to execute.
                  arguments: arguments for the execution depending on the lifecycle to execute.
                  properties: key=value configuration of the lifecycle.
                  
                Concatenate:
                  To concatenate multiple lifecycles enable concatenation with the option '-co'. To start a new lifecycle use 
                  a slash '-'. To cleanup all options for a new lifecycle use '--'.
                  To disable concatenion (will ignore - and --) use the -noco option.
                  con -co [options/properties] <lifecycle name> [arguments] - [options/properties] <lifecycle name> [arguments] ...
                """);

        for (Entry<String, MainOptionHandler> handler :
                new TreeMap<>(cli.getOptions()).entrySet()) {
            String usage = handler.getValue().getUsage(handler.getKey());
            String desc = handler.getValue().getDescription(handler.getKey());

            if (usage == null) usage = "";
            System.out.println(" " + handler.getKey() + " " + usage);
            if (desc != null) {
                desc = desc.replaceAll("(\\r\\n|\\n)", "\n     ");
                System.out.println("     " + desc);
            }
        }
    }

    @Override
    public String getDescription(String cmd) {
        return "Print help";
    }

    @Override
    public String getUsage(String cmd) {
        return null;
    }
}
