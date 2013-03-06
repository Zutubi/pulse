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
