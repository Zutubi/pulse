package com.zutubi.pulse.core;

import com.zutubi.pulse.core.model.CommandResult;
import com.zutubi.pulse.core.model.StoredArtifact;
import com.zutubi.validation.annotations.Name;
import com.zutubi.validation.annotations.Required;

/**
 * A special kind of artifact: a link to an external file.  Used to allow
 * one-stop-access to artifacts via the Pulse UI, without requiring all
 * artifacts to be captured and served by Pulse.
 */
public class LinkArtifact implements Artifact
{
    private String name;
    private String url;

    public LinkArtifact()
    {

    }

    public void capture(CommandResult result, CommandContext context)
    {
        result.addArtifact(new StoredArtifact(name, url));
    }

    @Required @Name public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    @Required public String getUrl()
    {
        return url;
    }

    public void setUrl(String url)
    {
        this.url = url;
    }
}
