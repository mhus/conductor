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
package de.mhus.con.plugin;

import java.io.File;

import de.mhus.con.api.AMojo;
import de.mhus.con.api.ConUtil;
import de.mhus.con.api.Context;
import de.mhus.con.api.ExecutePlugin;
import de.mhus.con.api.Project;
import de.mhus.lib.core.MLog;
import de.mhus.lib.core.MProperties;

@AMojo(name = "persistNewVersions")
public class PersistNewVersions extends MLog implements ExecutePlugin {

    @Override
    public boolean execute(Context context) throws Exception {
        String historyFilePath =
                context.getStep().getProperties().getString("historyFile", "history.properties");
        File historyFile = ConUtil.getFile(context.getConductor().getRoot(), historyFilePath);
        MProperties history = new MProperties();
        if (historyFile.exists() && historyFile.isFile()) history = MProperties.load(historyFile);
        for (Project project : context.getConductor().getProjects()) {
            String name = project.getName();
            String version = project.getProperties().getString("version", null);
            if (version != null) history.setString(name, version);
        }

        log().t("persist", history, historyFile);
        history.save(historyFile);

        return true;
    }
}
