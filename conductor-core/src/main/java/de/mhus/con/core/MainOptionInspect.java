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
import java.util.Map;
import java.util.Map.Entry;

import de.mhus.con.api.AOption;
import de.mhus.con.api.Cli;
import de.mhus.con.api.Conductor;
import de.mhus.con.api.ConfigType;
import de.mhus.con.api.Lifecycle;
import de.mhus.con.api.MainOptionHandler;
import de.mhus.con.api.Plugin;
import de.mhus.con.api.Project;
import de.mhus.con.api.Scheme;
import de.mhus.con.api.Step;
import de.mhus.con.api.Validator;

@AOption(alias = "-inspect")
public class MainOptionInspect implements MainOptionHandler {

    @Override
    public void execute(Cli cli, String cmd, LinkedList<String> queue) {

        String what = "all";
        if (!queue.isEmpty()) what = queue.removeFirst();

        if (what.equals("environment")) inspectEnvironment();
        else if (what.equals("cli")) inspectCli(cli);
        else if (what.equals("projects")) inspectProjects(cli);
        else if (what.equals("lifecycles")) inspectLifecycles(cli);
        else if (what.equals("conductor")) inspectConductor(cli);
        else if (what.equals("plugins")) inspectPlugins(cli);
        else if (what.equals("all")) {
            cli.getConductor(); // init conductor
            inspectEnvironment();
            System.out.println("CLI:");
            inspectCli(cli);
            System.out.println();
            System.out.println("CONDUCTOR:");
            inspectConductor(cli);
            System.out.println();
            System.out.println("PROJECTS:");
            inspectProjects(cli);
            System.out.println();
            System.out.println("LIFECYCLES:");
            inspectLifecycles(cli);
            System.out.println("PLUGINS:");
            inspectPlugins(cli);
        } else System.err.println("Unknow scope");
    }

    private void inspectEnvironment() {
        System.out.println("System Properties:");
        for (Entry<Object, Object> entry : System.getProperties().entrySet()) {
            System.out.println("    " + entry.getKey() + "=" + entry.getValue());
        }
        System.out.println();
        System.out.println("Environment");
        for (Map.Entry<?, ?> entry : System.getenv().entrySet()) {
            System.out.println("    " + entry.getKey() + "=" + entry.getValue());
        }
    }

    private void inspectPlugins(Cli cli) {
        for (Plugin plugin : cli.getConductor().getPlugins()) {
            System.out.println(">>> Target: " + plugin.getTarget());
            System.out.println("    Uri   : " + plugin.getUri());
            System.out.println("    Mojo  : " + plugin.getMojo());
        }
    }

    private void inspectConductor(Cli cli) {
        System.out.println("Properties:");
        for (Entry<String, Object> entry : cli.getConductor().getProperties().entrySet())
            System.out.println("    " + entry.getKey() + "=" + entry.getValue());
    }

    private void inspectLifecycles(Cli cli) {
        for (Lifecycle lifecycle : cli.getConductor().getLifecycles()) {
            System.out.println(">>> Lifecycle: " + lifecycle.getName());
            for (Step step : lifecycle.getSteps()) {
                System.out.println("    - Step      : " + step.getTitle());
                System.out.println("      Target    : " + step.getTarget());
                System.out.println("      Condition : " + step.getCondition());
                System.out.println("      Selector  : " + step.getSelector());
                System.out.println(
                        "      Order     : "
                                + step.getSortBy()
                                + " "
                                + (step.isOrderAsc() ? "ASC" : "DESC"));
                System.out.println("      Properties: " + step.getProperties());
            }
        }
    }

    private void inspectProjects(Cli cli) {
        Conductor con = cli.getConductor();
        for (Project project : con.getProjects()) {
            System.out.println(">>> " + project.getName());
            System.out.println("    Directory : " + project.getRootDir());
            System.out.println("    Path      : " + project.getPath());
            System.out.println("    Labels    : " + project.getLabels());
            System.out.println("    Properties: " + project.getProperties());
        }
    }

    private void inspectCli(Cli cli) {
        System.out.println("Schemes:");
        for (Entry<String, Scheme> entry : cli.getSchemes().entrySet())
            System.out.println(
                    entry.getKey() + ": " + entry.getValue().getClass().getCanonicalName());

        System.out.println();
        System.out.println("Config Types:");
        for (Entry<String, ConfigType> entry : cli.getConfigTypes().entrySet())
            System.out.println(
                    entry.getKey() + ": " + entry.getValue().getClass().getCanonicalName());

        System.out.println();
        System.out.println("Validators:");
        for (Entry<String, Validator> entry : cli.getValidators().entrySet())
            System.out.println(
                    entry.getKey() + ": " + entry.getValue().getClass().getCanonicalName());

        System.out.println();
        System.out.println("Options:");
        for (Entry<String, MainOptionHandler> entry : cli.getOptions().entrySet())
            System.out.println(
                    entry.getKey() + ": " + entry.getValue().getClass().getCanonicalName());
    }

    @Override
    public String getUsage(String cmd) {
        return "[projects|cli|lifecycles]";
    }

    @Override
    public String getDescription(String cmd) {
        return "Inspect current conductor configuration";
    }
}
