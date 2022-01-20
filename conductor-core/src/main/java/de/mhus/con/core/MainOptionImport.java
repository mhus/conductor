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
/*#man option
 *#title: Include additional configuration file
 *
 * Include an additional configuration file before processing the project configuration file.
 * 
 * Use this option if you project do not own a separate configuration file and the default
 * configuration should be used with additional functionality.
 * 
 * Usage:: -i import-uri
 * 
 * If schema is not found the schema 'default' will be added by default.
 * 
 */
package de.mhus.con.core;

import java.util.LinkedList;

import de.mhus.con.api.AOption;
import de.mhus.con.api.Cli;
import de.mhus.con.api.MainOptionHandler;

@AOption(alias = "-i")
public class MainOptionImport implements MainOptionHandler {

    @Override
    public void execute(Cli cli, String cmd, LinkedList<String> queue) {
        String incl = queue.removeFirst();
        if (incl.indexOf(':') < 0)
            incl = "default:" + incl;
        ((MainCli)cli).defaultImports.add(incl);
    }

    @Override
    public String getUsage(String cmd) {
        return "<import>";
    }

    @Override
    public String getDescription(String cmd) {
        return "Import additional configuration";
    }

}
