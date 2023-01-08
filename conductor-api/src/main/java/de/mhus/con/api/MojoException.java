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
package de.mhus.con.api;

import org.summerclouds.common.core.error.MException;
import org.summerclouds.common.core.error.RC;

public class MojoException extends MException {

    private static final long serialVersionUID = 1L;
    private Context context;

    public MojoException(Context context, Object... in) {
        super(RC.STATUS.INTERNAL_ERROR, in);
        this.context = context;
    }

    @Override
    public String toString() {
        return (context == null
                        ? ""
                        : "step="
                                + context.getStep()
                                + "\nproject="
                                + context.getProject()
                                + "\nplugin="
                                + context.getPlugin()
                                + "\n")
                + "message="
                + super.toString();
    }
}
