package com.zutubi.pulse.webwork.mapping;

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
