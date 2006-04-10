package com.zutubi.pulse.web;

import com.zutubi.pulse.ResourceNameComparator;
import com.zutubi.pulse.core.model.Resource;
import com.zutubi.pulse.model.persistence.ResourceDao;

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
