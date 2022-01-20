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

import de.mhus.con.api.AOption;
import de.mhus.con.api.Cli;
import de.mhus.con.api.ConUtil;
import de.mhus.con.api.MainOptionHandler;
import de.mhus.lib.core.MProperties;

@AOption(alias = {"-y", "-!y"})
public class MainOptionY implements MainOptionHandler {

    @Override
    public void execute(Cli cli, String cmd, LinkedList<String> queue) {
        ((MainCli) cli).getOverlayProperties().setBoolean(ConUtil.PROPERTY_Y, cmd.equals("-y"));
        if (((MainCli)cli).isConductor())
            ((MProperties)cli.getConductor().getProperties()).setBoolean(ConUtil.PROPERTY_Y, cmd.equals("-y"));
    }

    @Override
    public String getUsage(String cmd) {
        return "";
    }

    @Override
    public String getDescription(String cmd) {
        return "Automatically confirm questions with 'Yes'";
    }
}
