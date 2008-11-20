package com.zutubi.pulse.master.webwork.dispatcher.mapper.browse;

import com.zutubi.pulse.master.webwork.dispatcher.mapper.ActionResolver;
import com.zutubi.pulse.master.webwork.dispatcher.mapper.ActionResolverSupport;

import java.util.Arrays;
import java.util.List;

/**
 * Resolves to the artifacts view for a build.
 */
public class BuildArtifactsActionResolver extends ActionResolverSupport
{
    public BuildArtifactsActionResolver()
    {
        super("viewBuildArtifacts");
    }

    public List<String> listChildren()
    {
        return Arrays.asList("<stage>");
    }

    public ActionResolver getChild(String name)
    {
        return new StageArtifactsActionResolver(name, false);
    }
}
