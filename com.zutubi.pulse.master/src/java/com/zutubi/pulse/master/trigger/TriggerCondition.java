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

package com.zutubi.pulse.master.trigger;

import com.zutubi.pulse.master.model.Project;
import com.zutubi.pulse.master.tove.config.project.triggers.TriggerConditionConfiguration;

/**
 * Trigger conditions are applied before deciding to fire off a build from a
 * trigger.  They may be used to filter out unwanted firings.
 */
public interface TriggerCondition
{
    /**
     * Retrieves the configuration for this condition.
     *
     * @return the configuraiton for this condition
     */
    TriggerConditionConfiguration getConfig();

    /**
     * Applies this condition, indicating if it is satisfied and will allow a
     * build to be triggered.
     *
     * @param project the project that the trigger belongs to
     * @return true if this condition is satisfied
     */
    boolean satisfied(Project project);
}
