package com.zutubi.pulse.master.webwork.dispatcher.mapper.browse;

import com.zutubi.pulse.master.webwork.dispatcher.mapper.StaticMapActionResolver;

/**
 * Resolves actions for a build result.
 */
public class BuildActionResolver extends StaticMapActionResolver
{
    public BuildActionResolver(String id)
    {
        super("viewBuild");

        addMapping("summary", new BuildSummaryActionResolver());
        addMapping("details", new BuildDetailsActionResolver());
        addMapping("logs", new BuildLogsActionResolver());
        addMapping("changes", new BuildChangesActionResolver());
        addMapping("tests", new BuildTestsActionResolver());
        addMapping("file", new BuildPulseFileActionResolver());
        addMapping("artifacts", new BuildArtifactsActionResolver());
        addMapping("downloads", new BuildDownloadsActionResolver());

        addParameter("buildVID", id);
    }
}
