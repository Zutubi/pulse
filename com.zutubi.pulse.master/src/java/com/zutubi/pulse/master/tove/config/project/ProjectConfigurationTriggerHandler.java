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

package com.zutubi.pulse.master.tove.config.project;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.zutubi.pulse.master.tove.config.project.triggers.FireableTriggerConfiguration;
import com.zutubi.pulse.master.tove.config.project.triggers.ManualTriggerConfiguration;
import com.zutubi.pulse.master.tove.config.project.triggers.TriggerUtils;
import com.zutubi.tove.ui.model.ActionModel;

import java.util.List;

/**
 * Custom action handler for project triggers.
 */
public class ProjectConfigurationTriggerHandler extends AbstractTriggerHandler
{
    @Override
    protected ProjectConfiguration getProjectConfig(String path)
    {
        ProjectConfiguration project = configurationProvider.get(path, ProjectConfiguration.class);
        if (project == null)
        {
            throw new IllegalArgumentException("Path '" + path + "' does not exist");
        }
        return project;
    }

    @Override
    protected ManualTriggerConfiguration getTriggerConfig(String path, final String variant, ProjectConfiguration project)
    {
        List<ManualTriggerConfiguration> triggers = TriggerUtils.getTriggers(project, ManualTriggerConfiguration.class);
        Optional<ManualTriggerConfiguration> oTrigger = Iterables.tryFind(triggers, new Predicate<ManualTriggerConfiguration>()
        {
            @Override
            public boolean apply(ManualTriggerConfiguration input)
            {
                return input.getName().equals(variant);
            }
        });

        if (!oTrigger.isPresent())
        {
            throw new IllegalArgumentException("Project '" + project.getName() + "' has no manual trigger named '" + variant + "'");
        }

        return oTrigger.get();
    }

    @Override
    public ActionModel buildModel(FireableTriggerConfiguration trigger)
    {
        return new ActionModel("trigger", trigger.getName(), trigger.getName(), trigger.prompt());
    }
}
