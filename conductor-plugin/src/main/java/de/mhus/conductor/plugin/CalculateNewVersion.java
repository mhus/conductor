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
import de.mhus.conductor.core.LabelsImpl;
import lombok.extern.slf4j.Slf4j;

import java.io.File;

@Slf4j
@AMojo(name = "calculateNewVersion")
public class CalculateNewVersion  implements ExecutePlugin {

    @Override
    public boolean execute(Context context) throws Exception {
        String versionsFilePath =
                context.getStep().getProperties().getString("versionsFile", "versions.properties");
        File versionsFile = ConUtil.getFile(context.getConductor().getRoot(), versionsFilePath);
        LOGGER.trace("versions file: {}", versionsFile);
        if (versionsFile.exists() && versionsFile.isFile()) {
            MProperties versions = MProperties.load(versionsFile);

            // load history
            String historyFilePath =
                    context.getStep()
                            .getProperties()
                            .getString("historyFile", "history.properties");
            File historyFile = ConUtil.getFile(context.getConductor().getRoot(), historyFilePath);
            LOGGER.trace("histroy file: {}", historyFile);
            MProperties history = new MProperties();
            if (historyFile.exists() && historyFile.isFile())
                history = MProperties.load(historyFile);

            LOGGER.trace("versions: {}, history: {}", versions, history);

            // find versions for projects
            for (Project project : context.getConductor().getProjects()) {
                boolean changed = true;
                String name = project.getName();
                String version = versions.getString(name, null);
                if (version != null && version.equals("0.0.0")) {
                    LOGGER.info("Ignore project '{}' version",project.getName());
                    changed = false;
                } else
                if (version == null) {
                    version = history.getString(name, null);
                    changed = false;
                    LOGGER.debug("version from history, name: {}, version: {}", name, version);
                } else if (version.equals(history.getString(name, null))) {
                    changed = false;
                    LOGGER.debug("version not changed, name: {}, version: {}", name, version);
                }
                if (version == null) {
                     LOGGER.warn("project version not found for name: {}", name);
                } else {
                    if (changed) {
                        LOGGER.info("Project changed for name: {}, version: {}", name, version);
                    }
                    ((MProperties) project.getProperties()).setString("version", version);
                    ((LabelsImpl) project.getLabels())
                            .put("version.changed", String.valueOf(changed));
                }
            }

        } else {
            LOGGER.info("versions file not found: {}", versionsFile);
        }
        return true;
    }
}
