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

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.UUID;

import de.mhus.con.api.AOption;
import de.mhus.con.api.Cli;
import de.mhus.con.api.ConUtil;
import de.mhus.con.api.Conductor;
import de.mhus.con.api.Lifecycle;
import de.mhus.con.api.MainOptionHandler;
import de.mhus.con.api.Project;
import de.mhus.con.api.Step;
import de.mhus.lib.core.M;
import de.mhus.lib.core.MCast;
import de.mhus.lib.core.MProperties;
import de.mhus.lib.core.MString;
import de.mhus.lib.core.console.Console;
import de.mhus.lib.core.node.NodeList;
import de.mhus.lib.core.node.INode;
import de.mhus.lib.core.node.INodeFactory;
import de.mhus.lib.core.node.MNode;
import de.mhus.lib.core.yaml.MYaml;
import de.mhus.lib.core.yaml.YList;
import de.mhus.lib.errors.MException;

@AOption(alias = "-console")
public class MainOptionConsole implements MainOptionHandler {

    private Cli cli;
    private ExecutorImpl exec;
    private Console console;
    private LinkedList<String> historySteps;

    public void init(Cli cli) {
        this.cli = cli;
        exec = new ExecutorImpl();
        console = ConUtil.getConsole();
        historySteps = new LinkedList<String>();
    }

    @Override
    public void execute(Cli cli, String cmd, LinkedList<String> queue) {
        init(cli);
        try {
            LinkedList<String> history = new LinkedList<String>();

            console.println("Conductor console");
            while (true) {
//                console.setColor(COLOR.GREEN, null);
//                console.print("> ");
//                console.cleanup();
//                console.flush();
                String line = console.readLine("> ", history);
                try {
                    if (line == null || line.equals("quit")) break;
                    execute(line);
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            }
        } finally {
            cli = null;
        }
    }

    public void execute(String line) throws MException {
        if (MString.isEmptyTrim(line)) {
            // nothing
        } else if (line.startsWith("projects")) {
            if (line.trim().equals("projects")) {
                for (Project p : con().getProjects()) {
                    System.out.println(p.getName());
                    System.out.println("    " + p);
                }
            } else {
                LabelsImpl selector = new LabelsImpl();
                String[] parts = line.split(" ");
                String orderBy = null;
                for (int i = 1; i < parts.length; i++) {
                    String part = parts[i];
                    if (MString.isIndex(part, '=')) {
                        selector.put(
                                MString.beforeIndex(part, '='),
                                MString.afterIndex(part, '='));
                    } else {
                        orderBy = part;
                    }
                }
                List<Project> list = con().getProjects().select(selector);
                if (orderBy != null) {
                    ConUtil.orderProjects(list, orderBy, true);
                }
                for (Project p : list) {
                    System.out.println(p.getName());
                }
            }

        } else if (line.equals("lifecycles")) {
            for (Lifecycle lf : con().getLifecycles())
                System.out.println(
                        lf.getName() + "  -  " + lf.getSteps().size() + " Steps");
        } else if (line.startsWith("lifecycle")) {
            String[] parts = line.split(" ");
            String lifecycleName = parts[1].trim();
            Lifecycle lf = con().getLifecycles().get(lifecycleName);
            System.out.println(lifecycleName + ":");
            int cnt = 0;
            for (Step step : lf.getSteps()) {
                System.out.println(cnt + ": " + step.getTitle());
                System.out.println("    Target: " + step.getTarget());
                System.out.println("    " + step);
                cnt++;
            }
        } else if (line.startsWith("execute")) {
            String[] parts = line.split(" ");
            String lifecycleName = parts[1].trim();

            exec.setConductor(con());

            System.out.println(
                    "###################################################################");
            if (parts.length == 2) exec.execute(con(), lifecycleName);
            else {
                int stepNr = MCast.toint(parts[2], -1);
                Lifecycle lf = con().getLifecycles().get(lifecycleName);
                if (stepNr < 0 || stepNr >= lf.getSteps().size()) {
                    System.out.println("Index out of bounds");
                } else {
                    Step step = ((StepsImpl) lf.getSteps()).get(stepNr);
                    String name = UUID.randomUUID().toString();
                    StepsImpl steps = new StepsImpl();
                    steps.add(step);
                    LifecycleImpl lifecycle = new LifecycleImpl(name, steps);
                    ((LifecyclesImpl) con().getLifecycles()).put(name, lifecycle);
                    try {
                        exec.execute(con(), name);
                    } finally {
                        ((LifecyclesImpl) con().getLifecycles()).remove(name);
                    }
                }
            }
            System.out.println(
                    "###################################################################");
        } else if (line.trim().startsWith("- title:") || line.equals("steps")) {
            StringBuilder stepDef = new StringBuilder();
            if (!line.equals("steps")) {
                stepDef.append(line);
            } else {
                System.out.println("Finish the input with '---' in a single line:");
                while (true) {
//                    console.setColor(COLOR.RED, null);
//                    console.print("$ ");
//                    console.cleanup();
//                    console.flush();
                    String line2 = console.readLine("$ ", historySteps);
                    if (line2.equals("---")) break;
                    stepDef.append(line2).append('\n');
                }
            }
            System.out.println("Execute Steps:\n" + stepDef);

            String name = UUID.randomUUID().toString();
            StepsImpl steps = new StepsImpl();
            LifecycleImpl lifecycle = new LifecycleImpl(name, steps);

            ConfiguratorImpl config = new ConfiguratorImpl();
            YList executeE = MYaml.loadListFromString(stepDef.toString());
            config.loadSteps(executeE, steps);

            exec.setConductor(con());

            ((LifecyclesImpl) con().getLifecycles()).put(name, lifecycle);
            try {
                System.out.println(
                        "###################################################################");
                exec.execute(con(), name);
                System.out.println(
                        "###################################################################");
            } finally {
                ((LifecyclesImpl) con().getLifecycles()).remove(name);
            }
        } else if (line.startsWith("get ")) {
            System.out.println(con().getProperties().get(MString.afterIndex(line, ' ')));
        } else if (line.startsWith("print ")) {
            ContextImpl context = new ContextImpl(con(), 0);
            context.init(exec, new LinkedList<>(), null, new PluginImpl(), new StepImpl());
            System.out.println(context.make(MString.afterIndex(line, ' ')));
        } else if (line.equals("help")) {
            System.out.println("execute <name> [step nr]   - execute a lifecycle");
            System.out.println("lifecycles                 - list lifecycles");
            System.out.println("lifecycle <name>           - print lifecycle details");
            System.out.println(
                    "projects [key=value] [orderBy] - print or filter projects");
            System.out.println("env                        - print properties");
            System.out.println("<key>=<value>              - set property");
            System.out.println(
                    "steps                      - insert and execute a list of steps");
            System.out.println(
                    "- title:                   - insert and execute directly a list of steps (copy & paste only)");
            System.out.println(
                    "-<option>                  - execute option (e.g. --help)");
            System.out.println("quit                       - quit shell");
            System.out.println("backup <file>              - save property status");
            System.out.println("restore <file>             - load property status");
            System.out.println("print <key>                - print interpreted value from conductor");
            System.out.println("get <key>                  - print not interpreted value from conductor");
        } else if (line.startsWith("backup ")) {
            String file = MString.afterIndex(line, ' ').trim();
            if (!file.endsWith(".yaml")) file = file + ".yaml";
            System.out.println(">>> Save to " + file);
            MNode out = new MNode();
            Conductor con = cli.getConductor();
            {
                MNode p = new MNode();
                p.putReadProperties(con.getProperties());
                out.addObject("properties", p);
            }
            NodeList projectsC = out.getArrayOrCreate("projects");
            for (Project project : con.getProjects()) {
                MNode pp = new MNode();
                MNode p = new MNode();
                pp.setString("name", project.getName());
                p.putReadProperties(project.getProperties());
                pp.setObject("properties", p);
                projectsC.add(pp);
            }
            M.l(INodeFactory.class).write(out, new File(file));
        } else if (line.startsWith("restore ")) {
            String file = MString.afterIndex(line, ' ').trim();
            if (!file.endsWith(".yaml")) file = file + ".yaml";
            System.out.println(">>> Load from " + file);
            File f = new File(file);
            if (f.exists()) {
                INode in = M.l(INodeFactory.class).read(f);
                Conductor con = cli.getConductor();
                {
                    INode p = in.getObjectOrNull("properties");
                    if (p != null) {
                        ((MProperties) con.getProperties()).putReadProperties(p);
                    }
                }
                NodeList projectsC = in.getArrayOrCreate("projects");
                for (INode projectC : projectsC) {
                    String name = projectC.getString("name", null);
                    INode p = in.getObjectOrNull("properties");
                    if (name != null && p != null) {
                        Project project = con.getProjects().getOrNull(name);
                        if (project != null) {
                            ((MProperties) project.getProperties())
                                    .putReadProperties(p);
                        } else System.out.println("*** Project not found " + name);
                    }
                }
            } else {
                System.out.println("file not found");
            }
        } else if (line.equals("env")) {
            for (Entry<String, Object> entry : con().getProperties().entrySet())
                System.out.println(entry.getKey() + "=" + entry.getValue());
            for (Project p : con().getProjects()) {
                System.out.println("Project: " + p.getName());
                for (Entry<String, Object> entry : p.getProperties().entrySet())
                    System.out.println("  " + entry.getKey() + "=" + entry.getValue());
            }
        } else if (line.startsWith("-")) {
            String[] parts = line.split(" ");
            MainOptionHandler option = ((MainCli) cli).getOptions().get(parts[0]);
            if (option != null) {
                LinkedList<String> q = new LinkedList<>();
                for (int i = 1; i < parts.length; i++) q.add(parts[i]);
                option.execute(cli, parts[0], q);
            }
        } else if (line.contains("=")) {
            String k = MString.beforeIndex(line, '=');
            String v = MString.afterIndex(line, '=');
            ((MProperties) con().getProperties()).put(k, v);
        } else System.out.println("unknown command");
    }

    @Override
    public String getUsage(String cmd) {
        return null;
    }

    @Override
    public String getDescription(String cmd) {
        return "execute debug console";
    }

    public Conductor con() {
        return ((MainCli) cli).getConductor();
    }
}
