package com.zutubi.pulse.web.server;

import com.zutubi.pulse.model.Slave;
import com.zutubi.pulse.model.SlaveManager;
import com.zutubi.pulse.web.ActionSupport;

import java.util.List;

/**
 * An action to display all agents attached to this master, including the
 * master itself.
 */
public class ViewAgentsAction extends ActionSupport
{
    private List<Slave> slaves;
    private SlaveManager slaveManager;

    public List<Slave> getSlaves()
    {
        return slaves;
    }

    public String execute() throws Exception
    {
        slaves = slaveManager.getAll();
        return SUCCESS;
    }

    public void setSlaveManager(SlaveManager slaveManager)
    {
        this.slaveManager = slaveManager;
    }
}
