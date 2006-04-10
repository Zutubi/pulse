package com.zutubi.pulse.web;

import com.zutubi.pulse.core.model.Resource;
import com.zutubi.pulse.model.Slave;
import com.zutubi.pulse.model.SlaveManager;
import com.zutubi.pulse.model.persistence.ResourceDao;
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
