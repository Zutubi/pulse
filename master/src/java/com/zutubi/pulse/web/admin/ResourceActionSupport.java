package com.zutubi.pulse.web.admin;

import com.zutubi.pulse.web.ActionSupport;
import com.zutubi.pulse.web.agents.AgentActionSupport;
import com.zutubi.pulse.model.Slave;
import com.zutubi.pulse.model.SlaveManager;
import com.zutubi.pulse.model.PersistentResource;
import com.zutubi.pulse.model.ResourceManager;
import com.zutubi.pulse.core.model.Resource;
import com.zutubi.pulse.core.model.ResourceVersion;

/**
 */
public class ResourceActionSupport extends AgentActionSupport
{
    private long resourceId;
    protected Long versionId;
    private ResourceManager resourceManager;

    protected PersistentResource resource;
    protected ResourceVersion version;


    public long getResourceId()
    {
        return resourceId;
    }

    public void setResourceId(long resourceId)
    {
        this.resourceId = resourceId;
    }

    public Long getVersionId()
    {
        return versionId;
    }

    public void setVersionId(Long versionId)
    {
        this.versionId = versionId;
    }

    public Resource getResource()
    {
        return resource;
    }

    public ResourceVersion getVersion()
    {
        return version;
    }

    protected void lookupResource()
    {
        resource = resourceManager.findById(resourceId);
        if(resource == null)
        {
            addActionError("Unknown resource [" + resourceId + "]");
        }
        else
        {
            slave = resource.getSlave();
        }
    }

    protected void lookupVersion()
    {
        lookupVersion(false);
    }

    protected void lookupVersion(boolean required)
    {
        if(versionId != null)
        {
            version = resource.getVersion(versionId);
            if(version == null)
            {
                addActionError("Unknown version [" + versionId + "]");
            }
        }
        else if(required)
        {
            addActionError("Resource version required");
        }
    }

    public void setResourceManager(ResourceManager resourceManager)
    {
        this.resourceManager = resourceManager;
    }

    public ResourceManager getResourceManager()
    {
        return resourceManager;
    }
}
