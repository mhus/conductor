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
/*#man mojo
 *#title: Next Snapshot Version
 * 
 * Target: nextSnapshotVersions
 * Mojo: nextSnapshotVersions
 * Scope: Project
 * 
 * ===== Scope
 * 
 * The plugin calculates the new next snapshot version for a project. The property "version" must be set
 * in the project properties. It's used as base for the next version. The property "version" is overwritten
 * with the new version string. The original version string is stored in "version.original".
 * 
 * Version strings are a set of numbers, separated by dot and maybe a minus followed by a suffix.
 * e.g. 1.2.3-SNAPSHOT. The first number is the major, second the minor and last the hotfix counter.
 * 
 * ===== Usage 
 * 
 * The following step properties are used the will be searched recursively:
 * 
 * * nextVersionStatic: Set a static version string for the project (all other options will be ignored, "version" must not be set)
 * * nextVersionStep = major: Will increase the major version, set all other numbers to 0
 * * nextVersionStep = minor: Will increase the minor version, set all following numbers to 0
 * * nextVersionStep = hotfix: Will increase the hotfix version
 * * netVersionSuffix: Add a suffix to the new version. Default is 'SNAPSHOT'. Special values are #date, #timestamp and #buildnr.
 * 
 * * #date will add a date as string
 * * #isodate will add a date without time in iso format YYYYMMDD
 * * #timestamp will add the current timestamp. 
 * * #buildnr uses the previous number and increase it by one.
 * 
 */
package de.mhus.con.plugin;

import java.util.Date;

import de.mhus.con.api.AMojo;
import de.mhus.con.api.Context;
import de.mhus.con.api.ExecutePlugin;
import de.mhus.con.api.Project;
import de.mhus.con.core.ContextProject;
import de.mhus.lib.core.MCast;
import de.mhus.lib.core.MDate;
import de.mhus.lib.core.MLog;
import de.mhus.lib.core.MProperties;
import de.mhus.lib.core.MString;

@AMojo(name = "nextSnapshotVersions",target="nextSnapshotVersions")
public class NextSnapshotVersions extends MLog implements ExecutePlugin {

    @Override
    public boolean execute(Context context) throws Exception {
        Project project = context.getProject();
        String orgiginalVersion = ((MProperties) project.getProperties()).getString("version", null);
        String staticVersion = context.getRecursiveProperty("nextVersionStatic", null);
        String version = null;
        
        if (staticVersion != null) {
            if (orgiginalVersion != null && orgiginalVersion.equals("0.0.0"))
                log().i("Ignore project version",project.getName());
            else
                version = staticVersion;
        } else {
            String next = context.getRecursiveProperty("nextVersionStep", "minor").toLowerCase().trim();
            String suffix = context.getRecursiveProperty("netVersionSuffix", "SNAPSHOT");
            String vs = null;
            String v = orgiginalVersion;
            if (v != null && !v.equals("0.0.0")) {
                if (MString.isIndex(v, '-')) {
                    vs = MString.afterIndex(v, '-');
                    v = MString.beforeIndex(v, '-');
                }
                String[] parts = v.split("\\.");
                if ("major".equals(next)) {
                    parts[0] = String.valueOf(MCast.toint(parts[0], 0) + 1);
                    for (int i = 1; i < parts.length; i++)
                        parts[i] = "0";
                } else
                if ("minor".equals(next)) {
                    if (parts.length >= 2) {
                        parts[1] = String.valueOf(MCast.toint(parts[1], 0) + 1);
                        for (int i = 2; i < parts.length; i++)
                            parts[i] = "0";
                    }
                } else
                if ("hotfix".equals(next)) {
                    if (parts.length >= 3) {
                        parts[2] = String.valueOf(MCast.toint(parts[2], 0) + 1);
                        for (int i = 3; i < parts.length; i++)
                            parts[i] = "0";
                    }
                } else {
                    v = null; // trigger not to set new version
                }
                if (v != null) {
                    version = MString.join(parts, '.');
                    if (MString.isSet(suffix)) {
                        if (suffix.equals("#date"))
                            suffix = MDate.toIsoDate(new Date()).replace("-", "");
                        else
                        if (suffix.equals("#timestamp"))
                            suffix = String.valueOf(System.currentTimeMillis());
                        else
                        if (suffix.equals("#buildnr")) {
                            int nr = MCast.toint(vs, 0) + 1;
                            suffix = String.valueOf(nr);
                        }
                        version = version + "-" + suffix;
                    }
                }
            } else
                log().i("Ignore project version",project.getName());

        }

        if (version != null) {
            log().i("Next project version", project.getName(), version);
            ((MProperties) ((ContextProject) project).getInstance().getProperties())
                .setString("version", version);
            if (orgiginalVersion != null) {
                ((MProperties) ((ContextProject) project).getInstance().getProperties())
                .setString("version.original", orgiginalVersion);
            }
        }
        return true;
    }
}
