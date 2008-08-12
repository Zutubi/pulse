package com.zutubi.pulse.webwork.mapping.browse;

import com.zutubi.pulse.webwork.mapping.StaticMapActionResolver;

/**
 */
public class BuildActionResolver extends StaticMapActionResolver
{
    public BuildActionResolver(String id)
    {
        super("viewBuild");

        addMapping("summary", new BuildSummaryActionResolver());
        addMapping("details", new BuildDetailsActionResolver());
        addMapping("logs", new BuildLogsActionResolver());
        addMapping("log", new BuildLogActionResolver());
        addMapping("changes", new BuildChangesActionResolver());
        addMapping("tests", new BuildTestsActionResolver());
        addMapping("file", new BuildPulseFileActionResolver());
        addMapping("artifacts", new BuildArtifactsActionResolver());
        addMapping("wc", new BuildWorkingCopyActionResolver());

        addParameter("buildVID", id);
    }
}
