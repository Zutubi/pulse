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

import com.zutubi.pulse.master.tove.config.project.AbstractTriggerHandler;
import com.zutubi.pulse.master.tove.config.project.ProjectConfiguration;
import com.zutubi.tove.ui.model.ActionModel;

/**
 * Handler for firing triggers directly from their config pages.
 */
public class FireableTriggerConfigurationFireHandler extends AbstractTriggerHandler
{
    @Override
    protected ProjectConfiguration getProjectConfig(String path)
    {
        ProjectConfiguration project = configurationProvider.getAncestorOfType(path, ProjectConfiguration.class);
        if (project == null)
        {
            throw new IllegalArgumentException("Path '" + path + "' does not have an ancestor project");
        }
        return project;
    }

    @Override
    protected FireableTriggerConfiguration getTriggerConfig(String path, String variant, ProjectConfiguration project)
    {
        FireableTriggerConfiguration triggerConfig = configurationProvider.get(path, FireableTriggerConfiguration.class);
        if (triggerConfig == null)
        {
            throw new IllegalArgumentException("Path '" + path + "' does not exist or does not reference a fireable trigger");
        }

        return triggerConfig;
    }

    @Override
    protected ActionModel buildModel(FireableTriggerConfiguration trigger)
    {
        return new ActionModel(FireableTriggerConfigurationActions.ACTION_FIRE, "fire now", null, trigger.prompt());
    }
}
