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

 
import de.mhus.conductor.api.AMojo;
import de.mhus.conductor.api.Context;
import de.mhus.conductor.api.ExecutePlugin;
import de.mhus.conductor.api.Step;
import de.mhus.conductor.core.ContextStep;
import de.mhus.conductor.core.ExecutorImpl;
import lombok.extern.slf4j.Slf4j;

import java.io.Closeable;
import java.io.File;

@Slf4j
@AMojo(name = "projectKindSwitch",target = "project.kind")
public class ProjectKindSwitchMojo  implements ExecutePlugin {

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
                boolean success = false;
                LOGGER.debug("Found management type: {}",type);
                try ( Closeable x = ((ExecutorImpl)context.getExecutor()).enterSubSteps(context.getStep()) ) {
                    for (Step caze : typeCaze.getSubSteps()) {
                        ((ExecutorImpl)context.getExecutor()).executeInternal( ((ContextStep)caze).getInstance(), context.getProject(), context.getCallLevel()+1 );
                        if (context.getProject().getStatus() == de.mhus.conductor.api.Project.STATUS.SUCCESS) success = true;
                    }
                }
                return success;
            }
        }

        
        return false;
    }
    
}
