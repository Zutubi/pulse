/* Copyright 2017 Zutubi Pty Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.zutubi.pulse.master.model;

import com.zutubi.pulse.core.model.Entity;

/**
 * Represents a condition that filters project build notifications.  All
 * conditions boil down to a single boolean expression, however common are
 * expressions are configured with custom interfaces for usability.  The
 * condition objects capture the type of expression that has been configured.
 */
public abstract class ProjectBuildCondition extends Entity
{
    public abstract String getType();
    public abstract String getExpression();
}
