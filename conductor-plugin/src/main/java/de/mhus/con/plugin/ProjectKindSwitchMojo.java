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

import java.io.Closeable;
import java.io.File;

import de.mhus.con.api.AMojo;
import de.mhus.con.api.Context;
import de.mhus.con.api.ExecutePlugin;
import de.mhus.con.api.Step;
import de.mhus.con.core.ContextStep;
import de.mhus.con.core.ExecutorImpl;
import de.mhus.lib.core.MLog;

@AMojo(name = "projectKindSwitch",target = "kind")
public class ProjectKindSwitchMojo extends MLog implements ExecutePlugin {

    public enum TYPES {UNKNOWN,MAVEN,GRADEL,IVY,ANT,SBT,NPM,MAKE}
    
    @Override
    public boolean execute(Context context) throws Exception {

        File dir = context.getProject().getRootDir();
        TYPES type = TYPES.UNKNOWN;
        
        if (new File(dir, "pom.xml").exists()) {
            type = TYPES.MAVEN;
        } else
        if (new File(dir, "build.gradel").exists()) {
            type = TYPES.GRADEL;
        } else
        if (new File(dir, "build.sbt").exists()) {
            type = TYPES.SBT;
        } else
        if (new File(dir, "ivy.xml").exists()) {
            type = TYPES.IVY;
        } else
        if (new File(dir,"build.xml").exists()) {
            type = TYPES.ANT;
        } else
        if (new File(dir,"Makefile").exists()) {
            type = TYPES.MAKE;
        } else
        if (new File(dir, "package.json").exists()) {
            type = TYPES.NPM;
        }
        
        for (Step typeCaze : context.getStep().getSubSteps()) {
            if (typeCaze.getTarget().equalsIgnoreCase(type.name())) {
                log().d("Found management type",type);
                try ( Closeable x = ((ExecutorImpl)context.getExecutor()).enterSubSteps(context.getStep()) ) {
                    for (Step caze : typeCaze.getSubSteps()) {
                        ((ExecutorImpl)context.getExecutor()).executeInternal( ((ContextStep)caze).getInstance(), context.getProject(), context.getCallLevel()+1 );
                    }
                }
                return true;
            }
        }

        
        return false;
    }
    
}
