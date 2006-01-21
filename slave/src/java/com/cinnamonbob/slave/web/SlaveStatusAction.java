package com.cinnamonbob.slave.web;

import com.cinnamonbob.core.model.Resource;
import com.cinnamonbob.model.persistence.ResourceDao;
import com.opensymphony.xwork.ActionSupport;

import java.util.List;

/**
 * WW action for viewing the current server status.
 *
 * @author jsankey
 */
public class SlaveStatusAction extends ActionSupport
{
    private ResourceDao resourceDao;
    private List<Resource> resources;

    /**
     * Populates the action.
     */
    public String execute()
    {
        resources = resourceDao.findAll();
        return SUCCESS;
    }

    public List<Resource> getResources()
    {
        return resources;
    }

    public void setResourceDao(ResourceDao resourceDao)
    {
        this.resourceDao = resourceDao;
    }
}
