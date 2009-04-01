package com.zutubi.pulse.master.tove.config.project;

import com.zutubi.util.CollectionUtils;
import com.zutubi.util.Mapping;
import com.zutubi.util.StringUtils;

import java.util.List;

public class DependencyConfigurationFormatter
{
    public String getStages(DependencyConfiguration config)
    {
        if (config.isAllStages())
        {
            return "all stages";
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
}
