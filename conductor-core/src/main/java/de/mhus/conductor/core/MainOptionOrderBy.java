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
package de.mhus.conductor.core;

import de.mhus.commons.console.Console;
import de.mhus.commons.errors.MException;
import de.mhus.commons.services.MService;
import de.mhus.commons.tools.MCast;
import de.mhus.commons.tools.MString;
import de.mhus.commons.tree.ITreeNode;
import de.mhus.commons.tree.ITreeNodeFactory;
import de.mhus.commons.tree.MProperties;
import de.mhus.commons.tree.TreeNode;
import de.mhus.commons.tree.TreeNodeList;
import de.mhus.commons.yaml.MYaml;
import de.mhus.commons.yaml.YList;
import de.mhus.conductor.api.AOption;
import de.mhus.conductor.api.Cli;
import de.mhus.conductor.api.ConUtil;
import de.mhus.conductor.api.Conductor;
import de.mhus.conductor.api.Lifecycle;
import de.mhus.conductor.api.MainOptionHandler;
import de.mhus.conductor.api.Project;
import de.mhus.conductor.api.Step;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.UUID;

@AOption(alias = "-o")
public class MainOptionOrderBy implements MainOptionHandler {

    public void init(Cli cli) {
    }

    @Override
    public void execute(Cli cli, String cmd, LinkedList<String> queue) {
        if (((ConductorImpl)cli.getConductor()).getGeneralOrderBy() == null)
            ((ConductorImpl)cli.getConductor()).setGeneralOrderBy(new LinkedList<>());
        ((ConductorImpl)cli.getConductor()).getGeneralOrderBy().add(queue.removeFirst());
    }

    @Override
    public String getUsage(String cmd) {
        return "<order by>";
    }

    @Override
    public String getDescription(String cmd) {
        return "will override the general order by list for all steps, multiple times possible";
    }

}
