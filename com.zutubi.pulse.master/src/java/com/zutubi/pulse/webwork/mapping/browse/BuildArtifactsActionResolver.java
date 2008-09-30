package com.zutubi.pulse.webwork.mapping.browse;

import com.zutubi.pulse.webwork.mapping.ActionResolver;
import com.zutubi.pulse.webwork.mapping.ActionResolverSupport;

/**
 */
public class BuildArtifactsActionResolver extends ActionResolverSupport
{
    public BuildArtifactsActionResolver()
    {
        super("viewBuildArtifacts");
    }

    public ActionResolver getChild(String name)
    {
        return new StageArtifactsActionResolver(name);
    }
}
