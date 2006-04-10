package com.cinnamonbob.web;

import com.cinnamonbob.core.model.ResourceVersion;
import com.cinnamonbob.model.persistence.ResourceVersionDao;

/**
 * <class-comment/>
 */
public class DeleteResourceVersionAction extends ActionSupport
{
    private ResourceVersionDao resourceVersionDao;
    private long id;
    private long resourceId;
    private ResourceVersion resourceVersion;

    public void setId(long id)
    {
        this.id = id;
    }

    public long getId()
    {
        return id;
    }

    public long getResourceId()
    {
        return resourceId;
    }

    public void setResourceId(long resourceId)
    {
        this.resourceId = resourceId;
    }

    public void validate()
    {
        if (hasErrors())
        {
            return;
        }

        resourceVersion = resourceVersionDao.findById(id);
        if (resourceVersion == null)
        {
            addActionError("Unknown resource version '" + id + "'");
        }
    }

    public String execute()
    {
        resourceVersionDao.delete(resourceVersion);
        return SUCCESS;
    }

    public void setResourceVersionDao(ResourceVersionDao resourceVersionDao)
    {
        this.resourceVersionDao = resourceVersionDao;
    }
}
