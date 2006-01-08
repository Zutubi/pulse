package com.cinnamonbob.web;

import com.cinnamonbob.model.Slave;
import com.cinnamonbob.model.SlaveManager;
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

    /**
     * Populates the action.
     */
    public String execute()
    {
        slaves = slaveManager.getAll();
        return SUCCESS;
    }

    public List<Slave> getSlaves()
    {
        return slaves;
    }

    public void setSlaveManager(SlaveManager slaveManager)
    {
        this.slaveManager = slaveManager;
    }
}
