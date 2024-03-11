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

import de.mhus.common.core.log.MLog;
import de.mhus.common.core.node.IReadProperties;
import de.mhus.common.core.tool.MFile;
import de.mhus.common.core.tool.MXml;
import de.mhus.conductor.api.AMojo;
import de.mhus.conductor.api.Context;
import de.mhus.conductor.api.ExecutePlugin;
import de.mhus.conductor.api.Project;
import org.w3c.dom.*;

import java.io.File;

@AMojo(name = "updatePomProperties")
public class UpdatePomProperties extends MLog implements ExecutePlugin {

    @Override
    public boolean execute(Context context) throws Exception {

        IReadProperties pp = context.getProject().getProperties();

        File pomFile = new File(context.getProject().getRootDir(), "pom.xml");
        if (!pomFile.exists()) {
            log().i("pom.xml not found in project, skip");
            return false;
        }
        Document pomDoc = MXml.loadXml(pomFile);
        Element pomE = pomDoc.getDocumentElement();
        Element propE = MXml.getElementByPath(pomE, "properties");
        if (propE == null) {
            log().d("pom properties not found, skip");
            return false;
        }

        boolean changed = false;
        for (Project project : context.getConductor().getProjects()) {
            String version = project.getProperties().getString("version", null);
            String name = project.getName();
            if (context.getConductor().isVerboseOutput())
                System.out.println(">>> " + name + ":" + version);
            if (version != null) {
                String propProjectKey = "property." + name;
                String propKey = pp.getString(propProjectKey, name); // custom mapping
                Element propKeyE = MXml.getElementByPath(propE, propKey);
                if (context.getConductor().isVerboseOutput())
                    System.out.println("--- " + propProjectKey + "=" + propKey + "=" + propKeyE);
                if (propKeyE != null) {
                    String propValue = MXml.getValue(propKeyE, false);
                    if (context.getConductor().isVerboseOutput())
                        System.out.println("--- ==> " + propValue);
                    if (!version.equals(propValue)) {
                        changed = true;
                        // remove all
                        NodeList l = propKeyE.getChildNodes();
                        for (int i = 0; i < l.getLength(); i++) {
                            Node le = l.item(i);
                            propKeyE.removeChild(le);
                        }
                        Text versionE = pomDoc.createTextNode(version);
                        propKeyE.appendChild(versionE);
                    }
                }
            }
        }

        if (changed) {
            log().d("update pom", pomFile);
            String out = MXml.toString(pomDoc, false);
            out = out.replace("?><!--", "?>\n<!--").replace("--><project", "-->\n<project");
            MFile.writeFile(pomFile, out);
        }

        return true;
    }
}
