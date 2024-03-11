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

import de.mhus.conductor.api.AOption;
import de.mhus.conductor.api.Cli;
import de.mhus.conductor.api.ConUtil;
import de.mhus.conductor.api.MainOptionHandler;

import java.util.LinkedList;

@AOption(alias = {"-co","-concatenate"})
public class MainOptionConcatenate implements MainOptionHandler {

    @Override
    public void execute(Cli cli, String cmd, LinkedList<String> queue) {
        ((MainCli) cli).getOverlayProperties().setBoolean(ConUtil.PROPERTY_CONCATENATE, true);
    }

    @Override
    public String getUsage(String cmd) {
        return "";
    }

    @Override
    public String getDescription(String cmd) {
        return "Enable concatenation of lifecycles (parser will stop arguments for - and --";
    }

}
