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
import org.summerclouds.conductor.api.ConUtil;
import org.summerclouds.conductor.api.MainOptionHandler;

import java.util.LinkedList;

@AOption(alias = "-fae")
public class MainOptionFailAtEnd implements MainOptionHandler {

    @Override
    public void execute(Cli cli, String cmd, LinkedList<String> queue) {
        ((MainCli) cli).getOverlayProperties().put(ConUtil.PROPERTY_FAE, true);
    }

    @Override
    public String getUsage(String cmd) {
        return "";
    }

    @Override
    public String getDescription(String cmd) {
        return "Fail-At-End Option";
    }
}
