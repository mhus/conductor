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
@AMojo(name = "vcsKindSwitch",target = "vcs.kind")
public class VcsKindSwitchMojo implements ExecutePlugin {

    public enum TYPES {UNKNOWN,GIT,SUBVERSION}
    
    @Override
    public boolean execute(Context context) throws Exception {

        File dir = context.getProject().getRootDir();
        TYPES type = TYPES.UNKNOWN;
        
        if (new File(dir, ".git").exists()) {
            type = TYPES.GIT;
        } else
        if (new File(dir, ".svn").exists()) {
            type = TYPES.SUBVERSION;
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
                } catch (Exception e) {
                    LOGGER.error("Error in sub step: {}", type.name() , e);
                }
                return success;
            }
        }

        return false;
    }
    
}
