package com.zutubi.pulse.webwork.mapping.browse;

import com.zutubi.pulse.webwork.mapping.ActionResolver;
import com.zutubi.pulse.webwork.mapping.ActionResolverSupport;

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
