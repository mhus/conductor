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

 
import de.mhus.commons.tools.MString;
import de.mhus.commons.tools.MXml;
import de.mhus.conductor.api.ConUtil;
import de.mhus.conductor.api.Context;
import de.mhus.conductor.api.ExecutePlugin;
import lombok.extern.slf4j.Slf4j;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.File;
import java.util.LinkedList;

/**
 * Step Properties: * processSubModules (bool): Scan an process single sub modules (leafs) *
 * touchDirectory (bool): Every module/submodule must be touched * modifiedOnly (bool): Scan if
 * younger files then the root are included. If so the root folder will be touched after successful
 * execution
 *
 * @author mikehummel
 */
@Slf4j
public abstract class AbstractMavenExecute  implements ExecutePlugin {

    @Override
    public final boolean execute(Context context) throws Exception {

        // if not project scope - ignore mvn stuff
        if (context.getProject() == null) {
            return execute2(null, null, context);
        }

        LinkedList<String> modules = new LinkedList<>();
        // collect sub LEAF modules
        if (context.getStep().getProperties().getBoolean("processSubModules", false)) {
            // check for pom and modules
            if (hasModules(context.getProject().getRootDir())) {
                // collect modules
                collectLeafModules(context, modules, context.getProject().getRootDir(), "");
            }
        }
        // If no leaf use root as single module
        if (modules.size() == 0) modules.add("");

        boolean done = false;
        // iterate all modules
        for (String module : modules) {
            File dir = new File(context.getProject().getRootDir(), module);
            boolean needTouch =
                    context.getStep().getProperties().getBoolean("touchDirectory", false);
            // check if has younger
            if (context.getStep().getProperties().getBoolean("modifiedOnly", false)) {
                long lastModified = dir.lastModified();
                if (!hasYounger(dir, lastModified)) {
                    LOGGER.debug("not modified, skip module: {}", module);
                    continue;
                }
                needTouch = true;
            }
            if (modules.size() > 1)
                ConUtil.getConsole().println(">>> Execute Sub-Module: " + module);
            boolean taskDone = execute2(dir, module, context);
            if (taskDone) done = true;

            if (needTouch) {
                long time = System.currentTimeMillis();
                LOGGER.debug("Touch project folder: {}, time: {}", dir, time);
                dir.setLastModified(time);
            }
        }

        return done;
    }

    protected boolean hasYounger(File dir, long lastModified) {
        // TODO Configurable ignore list
        if (dir.getName().equals("target")
                || dir.getName().equals("bin")
                || dir.getName().startsWith(".")) return false;

        for (File file : dir.listFiles()) {
            if (file.isDirectory()) {
                if (hasYounger(file, lastModified)) return true;
            } else if (file.lastModified() > lastModified) {
                LOGGER.trace("Younger File", file);
                return true;
            }
        }
        return false;
    }

    private boolean hasModules(File rootDir) {
        File pomFile = new File(rootDir, "pom.xml");
        if (pomFile.isFile() && pomFile.exists()) {
            try {
                Document pomDoc = MXml.loadXml(pomFile);
                return pomDoc.getDocumentElement().getElementsByTagName("module").getLength() > 0;
            } catch (Throwable t) {
                LOGGER.debug("TODO", pomFile, t);
            }
        }
        return false;
    }
    /*
    <strings>
      <string>reactive-model</string>
      <string>reactive-util</string>
      <string>reactive-engine</string>
      <string>reactive-test</string>
      <string>reactive-osgi</string>
      <string>reactive-karaf</string>
      <string>reactive-dev</string>
    </strings>
     */
    protected boolean collectLeafModules(
            Context context, LinkedList<String> modules, File rootDir, String parentModulePath) {
        boolean isLeaf = true;
        if (hasModules(rootDir)) {
            try {
                String mvnPath = ConUtil.cmdLocation(context.getConductor(), "mvn");
                String cmd = mvnPath + " help:evaluate -Dexpression=project.modules";
                String[] res =
                        ConUtil.execute(
                                context.getConductor(),
                                context.getStep().getTitle() + " " + context.getProject().getName(),
                                rootDir,
                                cmd,
                                false);
                String content = res[0];
                int pos = content.indexOf("<strings>");
                if (pos > 0) {
                    content = content.substring(pos);
                    pos = content.indexOf("</strings>");
                    content = content.substring(0, pos + 10);
                    LOGGER.trace(content);
                    Element strE = MXml.loadXml(content).getDocumentElement();
                    for (String value : MXml.getValues(strE, "string")) {
                        isLeaf = false;
                        LOGGER.trace("Found module", parentModulePath + value);
                        if (collectLeafModules(
                                context,
                                modules,
                                new File(rootDir, value),
                                parentModulePath + value + "/")) {
                            // add only leafs
                            modules.add(parentModulePath + value);
                        }
                    }
                }
            } catch (Throwable t) {
                LOGGER.debug("TODO", rootDir, t);
            }
        }
        return isLeaf;
    }

    public String getCmdName(Context context, String moduleName) {
        return context.getStep().getTitle()
                + " "
                + context.getProject().getName()
                + (MString.isSet(moduleName) ? "/" + moduleName : "");
    }

    public abstract boolean execute2(File dir, String moduleName, Context context) throws Exception;
}
