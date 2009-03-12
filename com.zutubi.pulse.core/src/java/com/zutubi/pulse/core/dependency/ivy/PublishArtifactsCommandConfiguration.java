package com.zutubi.pulse.core.dependency.ivy;

import com.zutubi.pulse.core.commands.api.CommandConfigurationSupport;
import com.zutubi.pulse.core.RecipeRequest;

public class PublishArtifactsCommandConfiguration extends CommandConfigurationSupport
{
    private IvySupport ivy;
    private RecipeRequest request;
    
    public PublishArtifactsCommandConfiguration()
    {
        super(PublishArtifactsCommand.class);
    }

    public IvySupport getIvy()
    {
        return ivy;
    }

    public void setIvy(IvySupport ivy)
    {
        this.ivy = ivy;
    }

    public RecipeRequest getRequest()
    {
        return request;
    }

    public void setRequest(RecipeRequest request)
    {
        this.request = request;
    }
}
