package com.zutubi.pulse.master.webwork.mapping.browse;

import com.zutubi.pulse.master.webwork.mapping.ActionResolver;
import com.zutubi.pulse.master.webwork.mapping.ActionResolverSupport;

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
