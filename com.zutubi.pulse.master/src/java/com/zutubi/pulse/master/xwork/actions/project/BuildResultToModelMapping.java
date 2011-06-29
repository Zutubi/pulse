package com.zutubi.pulse.master.xwork.actions.project;

import com.zutubi.pulse.master.model.BuildResult;
import com.zutubi.pulse.master.tove.config.project.changeviewer.ChangeViewerConfiguration;
import com.zutubi.pulse.master.webwork.Urls;
import com.zutubi.util.Mapping;

import java.util.HashSet;
import java.util.Set;

/**
 * Maps from build results to JSON build models.
 */
public class BuildResultToModelMapping implements Mapping<BuildResult, BuildModel>
{
    private Urls urls;
    private ChangeViewerConfiguration changeViewerConfig;
    private Set<Long> collectIds = new HashSet<Long>();

    public BuildResultToModelMapping(Urls urls)
    {
        this(urls, null);
    }

    public BuildResultToModelMapping(Urls urls, ChangeViewerConfiguration changeViewerConfig)
    {
        this.urls = urls;
        this.changeViewerConfig = changeViewerConfig;
    }

    public BuildModel map(BuildResult buildResult)
    {
        boolean collectArtifacts = collectIds.contains(buildResult.getId());
        if (changeViewerConfig == null)
        {
            return new BuildModel(buildResult, urls, collectArtifacts);
        }
        else
        {
            return new BuildModel(buildResult, urls, collectArtifacts, changeViewerConfig);
        }
    }

    public void collectArtifactsForBuildId(long id)
    {
        collectIds.add(id);
    }
}
