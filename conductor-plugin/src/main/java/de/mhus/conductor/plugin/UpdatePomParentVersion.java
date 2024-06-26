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

import de.mhus.commons.errors.InternalException;
 
import de.mhus.commons.tools.MFile;
import de.mhus.commons.tools.MString;
import de.mhus.commons.tools.MXml;
import de.mhus.commons.tree.TreeNodeList;
import de.mhus.conductor.api.AMojo;
import de.mhus.conductor.api.Context;
import de.mhus.conductor.api.ExecutePlugin;
import lombok.extern.slf4j.Slf4j;
import org.w3c.dom.*;

import java.io.File;

@Slf4j
@AMojo(name = "updatePomParentVersion")
public class UpdatePomParentVersion  implements ExecutePlugin {

    @Override
    public boolean execute(Context context) throws Exception {

        File pomFile = new File(context.getProject().getRootDir(), "pom.xml");
        if (!pomFile.exists()) {
            LOGGER.info("pom.xml not found in project, skip");
            return false;
        }
        Document pomDoc = MXml.loadXml(pomFile);
        Element pomE = pomDoc.getDocumentElement();
        Element parentE = MXml.getElementByPath(pomE, "parent");
        if (parentE == null) {
            LOGGER.debug("pom parent not found, skip");
            return false;
        }
        Element versionE = MXml.getElementByPath(parentE, "version");
        if (versionE == null) {
            LOGGER.debug("pom parent version not found, skip");
            return false;
        }

        String version = context.getStep().getProperties().getString("version").get();
        LOGGER.trace("parent version: {}", version);
        if (MString.isEmptyTrim(version) || version.equals("null")) {
             LOGGER.warn("new parent version is empty, skip. Tip: set step property 'version'");
            return false;
        }
        if (MXml.getValue(versionE, false).equals(version)) {
            LOGGER.info("version not changed: {}", version);
            return false;
        }

        // remove all
        NodeList l = versionE.getChildNodes();
        for (int i = 0; i < l.getLength(); i++) {
            Node le = l.item(i);
            versionE.removeChild(le);
        }
        Text versionTE = pomDoc.createTextNode(version);
        versionE.appendChild(versionTE);

        LOGGER.debug("update pom file: {}", pomFile);
        String out = MXml.toString(pomDoc, false);
        out = out.replace("?><!--", "?>\n<!--").replace("--><project", "-->\n<project");
        MFile.writeFile(pomFile, out);
        return true;
    }
}
