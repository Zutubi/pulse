package com.cinnamonbob.web;

import com.cinnamonbob.core.model.Resource;
import com.cinnamonbob.core.model.ResourceVersion;
import com.cinnamonbob.model.persistence.ResourceDao;

/**
 * Used to add a new version to a resource.
 */
public class CreateResourceVersionAction extends ActionSupport
{
    private long resourceId;
    private Resource resource;
    private ResourceVersion resourceVersion = new ResourceVersion();
    private ResourceDao resourceDao;

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

    public ResourceVersion getResourceVersion()
    {
        return resourceVersion;
    }

    public void setResourceVersion(ResourceVersion resourceVersion)
    {
        this.resourceVersion = resourceVersion;
    }

    public String doInput()
    {
        return INPUT;
    }

    public void validate()
    {
        if (hasErrors())
        {
            // do not attempt to validate unless all other validation rules have 
            // completed successfully.
            return;
        }

        resource = resourceDao.findById(resourceId);
        if (resource == null)
        {
            addActionError("Unknown resource [" + resourceId + "]");
            return;
        }

        if (resource.hasVersion(resourceVersion.getValue()))
        {
            addFieldError("resourceVersion.value", "this resource already has a version '" + resourceVersion.getValue() + "'");
        }
    }

    public String execute()
    {
        resource.add(resourceVersion);
        resourceDao.save(resource);

        return SUCCESS;
    }

    public void setResourceDao(ResourceDao resourceDao)
    {
        this.resourceDao = resourceDao;
    }
}
