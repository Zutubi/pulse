package com.zutubi.pulse.webwork.mapping.browse;

import com.zutubi.pulse.webwork.mapping.ActionResolver;
import com.zutubi.pulse.webwork.mapping.ActionResolverSupport;
import com.zutubi.pulse.webwork.mapping.PathConsumingActionResolver;

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
        return new PathConsumingActionResolver("viewArtifactt", "path", name);
    }
}
