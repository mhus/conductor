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
import de.mhus.conductor.api.*;
import de.mhus.conductor.core.ConductorImpl;

import java.util.Map;

@AMojo(name = "con.validate")
public class ValidateMojo extends MLog implements ExecutePlugin {

    @Override
    public boolean execute(Context context) throws Exception {
        Map<String, Validator> validators =
                ((ConductorImpl) context.getConductor()).getValidators();
        for (String arg : context.getStep().getArguments()) {
            Validator validator = validators.get(arg);
            if (validator == null) throw new MojoException(context, "validator not found", arg);
            if (context.getProject() == null) {
                System.out.println(">>> Validate " + arg);
                validator.validate(context.getConductor());
            } else {
                System.out.println(">>> Validate " + context.getProject() + " with " + arg);
                validator.validate(context);
            }
        }
        return true;
    }
}
