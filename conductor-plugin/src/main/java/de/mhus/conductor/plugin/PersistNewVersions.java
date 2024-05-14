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
package de.mhus.conductor.plugin;

 
import de.mhus.commons.tree.MProperties;
import de.mhus.conductor.api.*;
import lombok.extern.slf4j.Slf4j;

import java.io.File;

@Slf4j
@AMojo(name = "persistNewVersions")
public class PersistNewVersions  implements ExecutePlugin {

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

        LOGGER.trace("persist: {}, file: {}", history, historyFile);
        history.save(historyFile);

        return true;
    }
}
