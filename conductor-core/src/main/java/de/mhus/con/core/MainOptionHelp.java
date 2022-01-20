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
package de.mhus.con.core;

import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.TreeMap;

import de.mhus.con.api.AOption;
import de.mhus.con.api.Cli;
import de.mhus.con.api.MainOptionHandler;

@AOption(alias = {"-help","-h"})
public class MainOptionHelp implements MainOptionHandler {

    @Override
    public void execute(Cli cli, String cmd, LinkedList<String> queue) {
        System.out.println("Arguments:");
        System.out.println(" <lifecycle> [property] ...");
        System.out.println(
                " Cascade [options] [arguments] [options] [arguments] ... to execute multiple lifecycles, use th empty Option '-' to separate.");
        System.out.println(
                " The arguments and options are in a queue. Define options then a lifecycle. If you start with options again (start with ',') then the lifecycle will be executed first befor the next option take effect. And the following lifecycle can be defined.");
        System.out.println();
        System.out.println("Property:");
        System.out.println(" key=value  - Set the value of the key");
        System.out.println(" key        - Set the key to true");
        System.out.println();
        System.out.println("Options:");
        System.out.println(" -");
        System.out.println(
                "     Dummy Option has not effect but will end a lifecycle definition and execute it.");
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
