package com.cinnamonbob.web;

import com.cinnamonbob.core.model.Resource;
import com.cinnamonbob.model.Slave;
import com.cinnamonbob.model.SlaveManager;
import com.cinnamonbob.model.persistence.ResourceDao;
import com.opensymphony.xwork.ActionSupport;

import java.util.List;

/**
 * WW action for viewing the current server status.
 *
 * @author jsankey
 */
public class ServerStatusAction extends ActionSupport
{
    private SlaveManager slaveManager;
    private List<Slave> slaves;
    private ResourceDao resourceDao;
    private List<Resource> resources;

    /**
     * Populates the action.
     */
    public String execute()
    {
        slaves = slaveManager.getAll();
        resources = resourceDao.findAll();
        return SUCCESS;
    }

    public List<Slave> getSlaves()
    {
        return slaves;
    }

    public List<Resource> getResources()
    {
        return resources;
    }

    public void setSlaveManager(SlaveManager slaveManager)
    {
        this.slaveManager = slaveManager;
    }

    public void setResourceDao(ResourceDao resourceDao)
    {
        this.resourceDao = resourceDao;
    }
}
