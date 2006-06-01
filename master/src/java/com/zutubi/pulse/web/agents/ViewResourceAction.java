package com.zutubi.pulse.web.agents;

import com.zutubi.pulse.core.model.Resource;
import com.zutubi.pulse.model.persistence.ResourceDao;
import com.zutubi.pulse.model.ResourceManager;
import com.zutubi.pulse.model.PersistentResource;

/**
 * 
 *
 */
public class ViewResourceAction extends AgentActionSupport
{
    private long resourceId;
    private PersistentResource resource;
    private ResourceManager resourceManager;

    public long getResourceId()
    {
        return resourceId;
    }

    public void setResourceId(long resourceId)
    {
        this.resourceId = resourceId;
    }

    public Resource getResource()
    {
        return resource;
    }

    public void setResource(PersistentResource resource)
    {
        this.resource = resource;
    }

    public String execute()
    {
        resource = resourceManager.findById(resourceId);
        if (resource == null)
        {
            addActionError("Unknown resource [" + resourceId + "]");
            return ERROR;
        }

        slave = resource.getSlave();
        return SUCCESS;
    }

    public void setResourceManager(ResourceManager resourceManager)
    {
        this.resourceManager = resourceManager;
    }
}
