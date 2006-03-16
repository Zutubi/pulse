package com.cinnamonbob.web;

import com.cinnamonbob.ResourceNameComparator;
import com.cinnamonbob.core.model.Resource;
import com.cinnamonbob.model.persistence.ResourceDao;

import java.util.Collections;
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
        Collections.sort(resources, new ResourceNameComparator());
        return SUCCESS;
    }

    public void setResourceDao(ResourceDao resourceDao)
    {
        this.resourceDao = resourceDao;
    }
}
