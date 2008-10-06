package com.zutubi.pulse.master.webwork.dispatcher.mapper.browse;

import com.zutubi.pulse.master.webwork.dispatcher.mapper.ActionResolver;
import com.zutubi.pulse.master.webwork.dispatcher.mapper.ActionResolverSupport;
import com.zutubi.pulse.master.webwork.dispatcher.mapper.PathConsumingActionResolver;

/**
 */
public class ArtifactActionResolver extends ActionResolverSupport
{
    public ArtifactActionResolver(String artifact)
    {
        super("viewBuildArtifacts");
        addParameter("artifactName", artifact);
    }

    public ActionResolver getChild(String name)
    {
        return new PathConsumingActionResolver("viewArtifact", "path", name);
    }
}
