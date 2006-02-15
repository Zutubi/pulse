package com.cinnamonbob.web;

import com.cinnamonbob.core.model.Resource;
import com.cinnamonbob.model.persistence.ResourceDao;

import java.util.List;

/**
 */
public class ViewResourcesAction extends ActionSupport
{
    private ResourceDao resourceDao;
    private List<Resource> resources;

    public List<Resource> getResources()
    {
        return resources;
    }

    public String execute() throws Exception
    {
        resources = resourceDao.findAll();
        return SUCCESS;
    }

    public void setResourceDao(ResourceDao resourceDao)
    {
        this.resourceDao = resourceDao;
    }
}
