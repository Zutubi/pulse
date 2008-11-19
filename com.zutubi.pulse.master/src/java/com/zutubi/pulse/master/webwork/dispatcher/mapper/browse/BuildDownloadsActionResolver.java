package com.zutubi.pulse.master.webwork.dispatcher.mapper.browse;

import com.zutubi.pulse.master.webwork.dispatcher.mapper.ActionResolver;
import com.zutubi.pulse.master.webwork.dispatcher.mapper.ActionResolverSupport;

/**
 * Support for raw artifact downloads.
 */
public class BuildDownloadsActionResolver extends ActionResolverSupport
{
    public BuildDownloadsActionResolver()
    {
        super("viewBuildArtifacts");
    }

    public ActionResolver getChild(String name)
    {
        return new StageArtifactsActionResolver(name, true);
    }
}