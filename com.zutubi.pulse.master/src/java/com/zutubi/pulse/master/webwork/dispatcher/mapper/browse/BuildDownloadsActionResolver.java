package com.zutubi.pulse.master.webwork.dispatcher.mapper.browse;

import com.zutubi.pulse.master.webwork.dispatcher.mapper.ActionResolver;
import com.zutubi.pulse.master.webwork.dispatcher.mapper.ActionResolverSupport;

import java.util.Arrays;
import java.util.List;

/**
 * Support for raw artifact downloads.
 */
public class BuildDownloadsActionResolver extends ActionResolverSupport
{
    public BuildDownloadsActionResolver()
    {
        super("viewBuildArtifacts");
    }

    public List<String> listChildren()
    {
        return Arrays.asList("<stage>");
    }

    public ActionResolver getChild(String name)
    {
        return new StageArtifactsActionResolver(name, true);
    }
}