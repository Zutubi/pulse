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

package com.zutubi.pulse.master.tove.config.project.triggers;

import static com.google.common.base.Predicates.instanceOf;
import com.google.common.collect.Iterables;
import static com.google.common.collect.Iterables.find;
import com.google.common.collect.Lists;
import static com.zutubi.pulse.master.tove.config.MasterConfigurationRegistry.EXTENSION_PROJECT_TRIGGERS;
import com.zutubi.pulse.master.tove.config.project.ProjectConfiguration;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class TriggerUtils
{
    /**
     * Get a trigger from the project configuration that is of the specified type.  If more than one such trigger
     * is present, this method returns the first trigger found.
     *
     * @param projectConfig the project configuration from which the trigger is being retrieved.
     * @param type          the type of the trigger being retrieved.
     * @param <T>           an extension of TriggerConfiguration.
     * @return a trigger instance of the specified type, or null if non exists.
     */
    public static <T extends TriggerConfiguration> T getTrigger(ProjectConfiguration projectConfig, Class<T> type)
    {
        Map<String, TriggerConfiguration> triggers = (Map<String, TriggerConfiguration>) projectConfig.getExtensions().get(EXTENSION_PROJECT_TRIGGERS);
        if (triggers != null)
        {
            return (T) find(triggers.values(), instanceOf(type), null);
        }
        return null;
    }

    /**
     * Get the triggers from the project configuration that are of the specified type.
     *
     * @param projectConfig the project configuration from which the triggers are being retrieved.
     * @param type          the type of the triggers being retrieved.
     * @param <T>           an extension of TriggerConfiguration.
     * @return a list of triggers of the specified type, or an empty list of no triggers of the
     * specified type exist.
     */
    public static <T extends TriggerConfiguration> List<T> getTriggers(ProjectConfiguration projectConfig, Class<T> type)
    {
        Map<String, TriggerConfiguration> triggers = (Map<String, TriggerConfiguration>) projectConfig.getExtensions().get(EXTENSION_PROJECT_TRIGGERS);
        if (triggers != null)
        {
            return Lists.newArrayList((Iterable<? extends T>) Iterables.filter(triggers.values(), instanceOf(type)));
        }
        return new LinkedList<T>();
    }
}
