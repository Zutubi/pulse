package com.zutubi.pulse.master.webwork.dispatcher.mapper.browse;

import com.zutubi.pulse.master.webwork.dispatcher.mapper.ActionResolver;
import com.zutubi.pulse.master.webwork.dispatcher.mapper.ActionResolverSupport;

/**
 * Maps to artifacts captured by a specific command.
 */
public class CommandArtifactsActionResolver extends ActionResolverSupport
{
    private boolean raw;

    public CommandArtifactsActionResolver(String command, boolean raw)
    {
        super("viewBuildArtifacts");
        addParameter("commandName", command);
        this.raw = raw;
    }

    public ActionResolver getChild(String name)
    {
        return new ArtifactActionResolver(name, raw);
    }
}
