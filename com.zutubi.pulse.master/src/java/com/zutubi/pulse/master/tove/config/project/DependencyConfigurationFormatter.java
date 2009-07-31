package com.zutubi.pulse.master.tove.config.project;

import com.zutubi.util.CollectionUtils;
import com.zutubi.util.Mapping;
import com.zutubi.util.StringUtils;
import com.zutubi.i18n.Messages;

import java.util.List;

/**
 * A custom formatter for the DependencyConfiguration object.
 */
public class DependencyConfigurationFormatter
{
    private static final Messages I18N = Messages.getInstance(DependencyConfigurationFormatter.class);

    /**
     * Format the stages field, a comma separated list of stage names (trimmed to 15 characters), or
     * 'all stages' if the all stages checkbox is selected.
     *
     * @param config instance being formatted.
     * @return the custom format string for the stages field.
     */
    public String getStages(DependencyConfiguration config)
    {
        if (config.isAllStages())
        {
            return I18N.format("all.label");
        }
        List<String> stageNames = CollectionUtils.map(config.getStages(), new Mapping<BuildStageConfiguration, String>()
        {
            public String map(BuildStageConfiguration stage)
            {
                return stage.getName();
            }
        });
        String joinedStageNames = StringUtils.join(", ", stageNames);
        return StringUtils.trimmedString(joinedStageNames, 15, "...");
    }

    public String getRevision(DependencyConfiguration config)
    {
        return config.getDependencyRevision();
    }
}
