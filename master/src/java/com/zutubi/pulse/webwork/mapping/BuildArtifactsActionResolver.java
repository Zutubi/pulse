package com.zutubi.pulse.webwork.mapping;

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
