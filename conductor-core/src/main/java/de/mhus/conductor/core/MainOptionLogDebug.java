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

import de.mhus.commons.tree.MProperties;
import de.mhus.conductor.api.AOption;
import de.mhus.conductor.api.Cli;
import de.mhus.conductor.api.ConUtil;
import de.mhus.conductor.api.MainOptionHandler;
import org.apache.logging.log4j.Level;

import java.util.LinkedList;

@AOption(alias = {"-v", "-vv", "-vvv","-!v","-vo"})
public class MainOptionLogDebug implements MainOptionHandler {

    @Override
    public void execute(Cli cli, String cmd, LinkedList<String> queue) {
        if (cmd.equals("-vo")) {
            ((MainCli)cli).getOverlayProperties().setBoolean(ConUtil.PROPERTY_VERBOSE, true);
            if (((MainCli)cli).isConductor())
                ((MProperties)cli.getConductor().getProperties()).setBoolean(ConUtil.PROPERTY_VERBOSE, true);
            return;
        }
        if (cmd.equals("-!v")) {
            MainCli.configureLog4jLevel(Level.INFO);
            ((MainCli)cli).getOverlayProperties().setBoolean(ConUtil.PROPERTY_VERBOSE, false);
            if (((MainCli)cli).isConductor())
                ((MProperties)cli.getConductor().getProperties()).setBoolean(ConUtil.PROPERTY_VERBOSE, false);
            return;
        }
        MainCli.configureLog4jLevel(cmd.equals("-vvv") ? Level.TRACE : Level.DEBUG);

        ((MainCli)cli).getOverlayProperties().setBoolean(ConUtil.PROPERTY_VERBOSE, true);
        if (((MainCli)cli).isConductor())
            ((MProperties)cli.getConductor().getProperties()).setBoolean(ConUtil.PROPERTY_VERBOSE, true);
    }

    @Override
    public String getDescription(String cmd) {
        return    "Enable Log DEBUG/TRACE levels.\n"
                + "-!v disables DEBUG\n"
                + "-vo enables only Verbose Output";
    }

    @Override
    public String getUsage(String cmd) {
        return null;
    }
}
