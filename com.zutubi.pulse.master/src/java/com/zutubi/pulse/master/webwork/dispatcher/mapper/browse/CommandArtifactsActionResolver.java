package com.zutubi.pulse.master.webwork.dispatcher.mapper.browse;

import com.zutubi.pulse.master.webwork.dispatcher.mapper.ActionResolver;
import com.zutubi.pulse.master.webwork.dispatcher.mapper.ActionResolverSupport;

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
