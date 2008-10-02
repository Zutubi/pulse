package com.zutubi.pulse.master.webwork.mapping.browse;

import com.zutubi.pulse.master.webwork.mapping.ActionResolver;
import com.zutubi.pulse.master.webwork.mapping.ActionResolverSupport;

/**
 */
public class StageArtifactsActionResolver extends ActionResolverSupport
{
    public StageArtifactsActionResolver(String stage)
    {
        super("viewBuildArtifacts");
        addParameter("stageName", stage);
    }

    public ActionResolver getChild(String name)
    {
        return new CommandArtifactsActionResolver(name);
    }
}
