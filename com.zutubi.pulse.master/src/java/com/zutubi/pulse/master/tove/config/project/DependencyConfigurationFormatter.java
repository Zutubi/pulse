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

import com.zutubi.i18n.Messages;
import com.zutubi.tove.config.api.Configurations;
import com.zutubi.util.StringUtils;

import static com.google.common.collect.Iterables.transform;

/**
 * A custom formatter for the DependencyConfiguration object.
 */
public class DependencyConfigurationFormatter
{
    private static final Messages I18N = Messages.getInstance(DependencyConfigurationFormatter.class);

    public String getProjectName(DependencyConfiguration config)
    {
        ProjectConfiguration project = config.getProject();
        return project == null ? "" : project.getName();
    }

    /**
     * Format the stages field, a comma separated list of stage names (trimmed to 15 characters), or
     * 'all stages' if the all stages checkbox is selected.
     *
     * @param config instance being formatted.
     * @return the custom format string for the stages field.
     */
    public String getStages(DependencyConfiguration config)
    {
        switch (config.getStageType())
        {
            case ALL_STAGES:
                return I18N.format("all.label");
            case CORRESPONDING_STAGES:
                return I18N.format("corresponding.label");
        }

        String joinedStageNames = StringUtils.join(", ", transform(config.getStages(), Configurations.toConfigurationName()));
        return StringUtils.trimmedString(joinedStageNames, 15);
    }

    public String getRevision(DependencyConfiguration config)
    {
        return config.getDependencyRevision();
    }
}
