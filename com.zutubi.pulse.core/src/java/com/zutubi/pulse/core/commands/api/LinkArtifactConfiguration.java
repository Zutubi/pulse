package com.zutubi.pulse.core.commands.api;

import com.zutubi.tove.annotations.Form;
import com.zutubi.tove.annotations.SymbolicName;
import com.zutubi.validation.annotations.Required;

/**
 * Configures a capture of a link to an external system.
 *
 * @see LinkArtifact
 */
@SymbolicName("zutubi.linkArtifactConfig")
@Form(fieldOrder = {"name", "url"})
public class LinkArtifactConfiguration extends ArtifactConfigurationSupport
{
    @Required
    private String url;

    public String getUrl()
    {
        return url;
    }

    public void setUrl(String url)
    {
        this.url = url;
    }

    public Class<? extends Artifact> artifactType()
    {
        return LinkArtifact.class;
    }
}
