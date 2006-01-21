package com.cinnamonbob.web;

import com.cinnamonbob.core.model.Resource;
import com.cinnamonbob.model.persistence.ResourceDao;

/**
 * <class-comment/>
 */
public class DeleteResourceAction extends ActionSupport
{
    private ResourceDao resourceDao;
    private long id;
    private Resource resource;

    public void setId(long id)
    {
        this.id = id;
    }

    public long getId()
    {
        return id;
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
        resourceDao.delete(resource);
        return SUCCESS;
    }

    public void setResourceDao(ResourceDao resourceDao)
    {
        this.resourceDao = resourceDao;
    }
}
