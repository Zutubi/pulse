package com.zutubi.pulse.master.xwork.actions.project;

import com.google.common.base.Function;
import com.zutubi.pulse.master.model.BuildResult;
import com.zutubi.pulse.master.tove.config.project.changeviewer.ChangeViewerConfiguration;
import com.zutubi.pulse.master.webwork.Urls;

import java.util.HashSet;
import java.util.Set;

/**
 * Maps from build results to JSON build models.
 */
public class BuildResultToModelFunction implements Function<BuildResult, BuildModel>
{
    private Urls urls;
    private ChangeViewerConfiguration changeViewerConfig;
    private Set<Long> collectIds = new HashSet<Long>();

    public BuildResultToModelFunction(Urls urls)
    {
        this(urls, null);
    }

    public BuildResultToModelFunction(Urls urls, ChangeViewerConfiguration changeViewerConfig)
    {
        this.urls = urls;
        this.changeViewerConfig = changeViewerConfig;
    }

    public BuildModel apply(BuildResult buildResult)
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
