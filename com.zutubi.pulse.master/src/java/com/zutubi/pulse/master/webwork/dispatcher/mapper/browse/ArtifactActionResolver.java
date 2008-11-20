package com.zutubi.pulse.master.webwork.dispatcher.mapper.browse;

import com.zutubi.pulse.master.webwork.dispatcher.mapper.ActionResolver;
import com.zutubi.pulse.master.webwork.dispatcher.mapper.ActionResolverSupport;
import com.zutubi.pulse.master.webwork.dispatcher.mapper.PathConsumingActionResolver;

import java.util.Arrays;
import java.util.List;

/**
 * Maps to either the raw download or decorated view of an artifact file.
 */
public class ArtifactActionResolver extends ActionResolverSupport
{
    private boolean raw;

    public ArtifactActionResolver(String artifact, boolean raw)
    {
        super("viewBuildArtifacts");
        addParameter("artifactName", artifact);
        this.raw = raw;
    }

    public List<String> listChildren()
    {
        return Arrays.asList("<artifact path>");
    }

    public ActionResolver getChild(String name)
    {
        return new PathConsumingActionResolver(raw ? "downloadArtifact" : "viewArtifact", "path", name);
    }
}
