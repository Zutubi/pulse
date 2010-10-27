package com.zutubi.pulse.master.xwork.actions.project;

import com.zutubi.pulse.master.model.BuildResult;
import com.zutubi.pulse.master.tove.config.project.changeviewer.ChangeViewerConfiguration;
import com.zutubi.util.Mapping;

/**
 * Maps from build results to JSON build models.
 */
class BuildResultToModelMapping implements Mapping<BuildResult, BuildModel>
{
    private ChangeViewerConfiguration changeViewerConfig;

    BuildResultToModelMapping(ChangeViewerConfiguration changeViewerConfig)
    {
        this.changeViewerConfig = changeViewerConfig;
    }

    public BuildModel map(BuildResult buildResult)
    {
        return new BuildModel(buildResult, changeViewerConfig);
    }
}
