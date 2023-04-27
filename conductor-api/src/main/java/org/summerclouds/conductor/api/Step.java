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
package org.summerclouds.conductor.api;

import org.summerclouds.common.core.node.IProperties;

import java.util.LinkedList;

public interface Step {

    LinkedList<String> getArguments();

    Labels getSelector();

    String[] getSortBy();

    String getTarget();

    String getCondition();

    boolean matchCondition(Context context);

    String getTitle();

    IProperties getProperties();

    int getId();

    Steps getSubSteps();
}
