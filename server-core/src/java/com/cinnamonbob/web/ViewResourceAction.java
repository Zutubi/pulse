package com.cinnamonbob.web;

import com.cinnamonbob.core.model.Resource;
import com.cinnamonbob.model.persistence.ResourceDao;

/**
 * 
 *
 */
public class ViewResourceAction extends ActionSupport
{
    private long id;
    private Resource resource;
    private ResourceDao resourceDao;

    public long getId()
    {
        return id;
    }

    public void setId(long id)
    {
        this.id = id;
    }

    public Resource getResource()
    {
        return resource;
    }

    public void setResource(Resource resource)
    {
        this.resource = resource;
    }

    public void validate()
    {
        if (hasErrors())
        {
            return;
        }

        resource = resourceDao.findById(id);
        if (resource == null)
        {
            addActionError("Unknown resource '" + id + "'");
        }
    }

    public String execute()
    {
        return SUCCESS;
    }

    public void setResourceDao(ResourceDao resourceDao)
    {
        this.resourceDao = resourceDao;
    }
}
