package com.zutubi.pulse.webwork.mapping;

/**
 */
public class CommandArtifactsActionResolver extends ActionResolverSupport
{
    public CommandArtifactsActionResolver(String command)
    {
        super("viewBuildArtifacts");
        addParameter("commandName", command);
    }

    public ActionResolver getChild(String name)
    {
        return new ArtifactActionResolver(name);
    }
}
