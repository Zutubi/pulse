package com.zutubi.pulse.web.agents;

import com.zutubi.pulse.model.Slave;
import com.zutubi.pulse.model.SlaveManager;
import com.zutubi.pulse.web.ActionSupport;
import com.zutubi.pulse.bootstrap.ConfigurationManager;

import java.util.List;

/**
 * An action to display all agents attached to this master, including the
 * master itself.
 */
public class ViewAgentsAction extends ActionSupport
{
    private List<Slave> slaves;
    private ConfigurationManager configurationManager;
    private SlaveManager slaveManager;

    public List<Slave> getSlaves()
    {
        return slaves;
    }

    public int getServerPort()
    {
        return configurationManager.getAppConfig().getServerPort();
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

    public void setConfigurationManager(ConfigurationManager configurationManager)
    {
        this.configurationManager = configurationManager;
    }
}
