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

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import de.mhus.con.api.AMojo;
import de.mhus.con.api.Context;
import de.mhus.con.api.ExecutePlugin;
import de.mhus.lib.core.MFile;
import de.mhus.lib.core.MLog;
import de.mhus.lib.core.MString;
import de.mhus.lib.core.MXml;
import de.mhus.lib.errors.MException;

@AMojo(name = "updatePomParentVersion")
public class UpdatePomParentVersion extends MLog implements ExecutePlugin {

    @Override
    public boolean execute(Context context) throws Exception {

        File pomFile = new File(context.getProject().getRootDir(), "pom.xml");
        if (!pomFile.exists()) {
            log().i("pom.xml not found in project, skip");
            return false;
        }
        Document pomDoc = MXml.loadXml(pomFile);
        Element pomE = pomDoc.getDocumentElement();
        Element parentE = MXml.getElementByPath(pomE, "parent");
        if (parentE == null) {
            log().d("pom parent not found, skip");
            return false;
        }
        Element versionE = MXml.getElementByPath(parentE, "version");
        if (versionE == null) {
            log().d("pom parent version not found, skip");
            return false;
        }

        String version = context.getStep().getProperties().getString("version");
        log().t("parent version", version);
        if (MString.isEmptyTrim(version))
            throw new MException("parent version is empty, skip. Tip: set step property 'version'");

        if (MXml.getValue(versionE, false).equals(version)) {
            log().i("version not changed", version);
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

        log().d("update pom", pomFile);
        String out = MXml.toString(pomDoc, false);
        out = out.replace("?><!--", "?>\n<!--").replace("--><project", "-->\n<project");
        MFile.writeFile(pomFile, out);
        return true;
    }
}
