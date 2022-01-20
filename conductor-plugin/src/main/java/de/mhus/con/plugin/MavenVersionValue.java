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
import java.io.IOException;
import java.util.Map;

import de.mhus.con.api.AMojo;
import de.mhus.con.api.ConUtil;
import de.mhus.con.api.Context;
import de.mhus.con.api.MojoException;
import de.mhus.con.api.ValuePlugin;
import de.mhus.lib.core.MLog;
import de.mhus.lib.core.MProperties;
import de.mhus.lib.core.MString;
import de.mhus.lib.core.logging.Log;
import de.mhus.lib.errors.NotFoundException;

@AMojo(name = "maven.version",target = "maven.version")
public class MavenVersionValue extends MLog implements ValuePlugin {

    private static MProperties versions;

    @Override
    public String getValue(Context context, String value, Map<String, Object> attributes) throws Exception {
        initMavenVersion(context,log());
        if (MString.isEmpty(value)) value = "maven.version";
        return versions.getString(value);
    }

    public static synchronized void initMavenVersion(Context context, Log log) throws NotFoundException, IOException, MojoException {
        if (versions != null) return;
        String mvnPath = ConUtil.cmdLocation(context.getConductor(), "mvn");
        String cmd = mvnPath + " --version";
        String[] res = ConUtil.execute(
                context.getConductor(),
                "maven version", 
                new File("."), 
                cmd, 
                false);
        if (!res[2].equals("0"))
            throw new MojoException(context, "not successful", cmd, res[1], res[2]);
        
        versions = new MProperties();
        
        try {
            String[] lines = res[0].split("\n");
            for (String line : lines) {
                if (line.startsWith("Apache Maven")) {
                    // Apache Maven 3.2.5 (12a6b3acb947671f09b81f49094c53f426d8cea1; 2014-12-14T18:29:23+01:00)
                    versions.setString("maven.version", line.split(" ")[2]);
                } else
                if (line.startsWith("OS name:")) {
                    // OS name: "linux", version: "4.12.14-94.41-default", arch: "amd64", family: "unix"
                    String[] parts = line.split("\"");
                    versions.setString("os.name", parts[1]);
                    versions.setString("os.version", parts[3]);
                    versions.setString("os.arch", parts[5]);
                    versions.setString("os.family", parts[7]);
                }
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

}
