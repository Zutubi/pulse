package com.cinnamonbob.web;

import com.cinnamonbob.BuildQueue;
import com.cinnamonbob.model.Slave;
import com.cinnamonbob.model.persistence.SlaveDao;
import com.opensymphony.xwork.ActionSupport;

import java.util.List;

/**
 * WW action for viewing the current server status.
 *
 * @author jsankey
 */
public class ServerStatusAction extends ActionSupport
{
    private BuildQueue buildQueue;
    private SlaveDao slaveDao;
    private List<Slave> slaves;

    /**
     * Populates the action.
     */
    public String execute()
    {
        slaves = slaveDao.findAll();
        return SUCCESS;
    }

    public BuildQueue getBuildQueue()
    {
        return buildQueue;
    }

    public void setBuildQueue(BuildQueue buildQueue)
    {
        this.buildQueue = buildQueue;
    }

    public void setSlaveDao(SlaveDao dao)
    {
        slaveDao = dao;
    }

    public List<Slave> getSlaves()
    {
        return slaves;
    }
}
