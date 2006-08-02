package com.zutubi.pulse.web.agents;

import com.zutubi.pulse.bootstrap.MasterConfigurationManager;
import com.zutubi.pulse.web.ActionSupport;

/**
 */
public class EditMasterAgentAction extends ActionSupport
{
    private MasterConfigurationManager configurationManager;
    private String agentHost;

    public String getAgentHost()
    {
        return agentHost;
    }

    public void setAgentHost(String agentHost)
    {
        this.agentHost = agentHost;
    }

    public String doInput()
    {
        agentHost = configurationManager.getAppConfig().getAgentHost();
        return INPUT;
    }

    public String execute()
    {
        configurationManager.getAppConfig().setAgentHost(agentHost);
        return SUCCESS;
    }

    public void setConfigurationManager(MasterConfigurationManager configurationManager)
    {
        this.configurationManager = configurationManager;
    }
}
