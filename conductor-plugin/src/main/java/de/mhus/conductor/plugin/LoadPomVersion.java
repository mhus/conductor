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
import de.mhus.commons.tools.MXml;
import de.mhus.conductor.api.AMojo;
import de.mhus.conductor.api.Context;
import de.mhus.conductor.api.ExecutePlugin;
import de.mhus.conductor.core.ContextProject;
import lombok.extern.slf4j.Slf4j;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.File;

@Slf4j
@AMojo(name = "loadPomVersion")
public class LoadPomVersion  implements ExecutePlugin {

    @Override
    public boolean execute(Context context) throws Exception {

        File pomFile = new File(context.getProject().getRootDir(), "pom.xml");
        if (!pomFile.exists()) {
            LOGGER.info("pom.xml not found in project, skip");
            return false;
        }

        String version = null;

        Document pomDoc = MXml.loadXml(pomFile);
        Element pomE = pomDoc.getDocumentElement();
        Element versionE = MXml.getElementByPath(pomE, "version");
        if (versionE != null) {
            version = MXml.getValue(versionE, false);
        } else {
            Element parentE = MXml.getElementByPath(pomE, "parent");
            if (parentE != null) {
                versionE = MXml.getElementByPath(pomE, "version");
                if (versionE != null) {
                    version = MXml.getValue(versionE, false);
                }
            }
        }

        if (version == null) {
             LOGGER.warn("Project version not found, skip");
            return false;
        }

        LOGGER.info("Project: {}, version: {}", context.getProject(), version);
        ((MProperties) ((ContextProject) context.getProject()).getInstance().getProperties())
                .setString("version", version);

        return true;
    }
}
