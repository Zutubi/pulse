package com.zutubi.pulse.web.admin;

import com.zutubi.pulse.model.PersistentResource;
import com.zutubi.pulse.model.ResourceManager;
import com.zutubi.pulse.web.agents.AgentActionSupport;

/**
 * <class-comment/>
 */
public class DeleteResourceAction extends AgentActionSupport
{
    private ResourceManager resourceManager;
    private long id;
    private PersistentResource resource;

    public void setId(long id)
    {
        this.id = id;
    }

    public long getId()
    {
        return id;
    }

    public String execute()
    {
        resource = resourceManager.findById(id);
        if (resource == null)
        {
            addActionError("Unknown resource '" + id + "'");
            return ERROR;
        }

        resourceManager.delete(resource);

        slave = resource.getSlave();
        if(slave != null)
        {
            setAgentId(slave.getId());
        }

        return SUCCESS;
    }

    public void setResourceManager(ResourceManager resourceManager)
    {
        this.resourceManager = resourceManager;
    }
}
