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
package de.mhus.con.core;

import de.mhus.con.api.Context;
import de.mhus.con.api.ErrorInfo;

public class ErrorsInfoImpl implements ErrorInfo {

    private ContextImpl context;
    private Throwable error;

    public ErrorsInfoImpl(ContextImpl context, Throwable t) {
        this.context = context;
        this.error = t;
    }

    @Override
    public Context getContext() {
        return context;
    }

    @Override
    public Throwable getError() {
        return error;
    }
}
