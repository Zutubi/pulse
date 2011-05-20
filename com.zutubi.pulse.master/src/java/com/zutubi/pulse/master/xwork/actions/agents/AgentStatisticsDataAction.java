package com.zutubi.pulse.master.xwork.actions.agents;

import com.zutubi.i18n.Messages;
import com.zutubi.pulse.master.agent.Agent;

/**
 * Action to return JSON data for the agent statistics tab.
 */
public class AgentStatisticsDataAction extends AgentActionBase
{
    private static final Messages I18N = Messages.getInstance(AgentStatisticsDataAction.class);

    private AgentStatisticsModel model;

    public AgentStatisticsModel getModel()
    {
        return model;
    }

    @Override
    public String execute() throws Exception
    {
        Agent agent = getRequiredAgent();
        model = new AgentStatisticsModel(agentManager.getAgentStatistics(agent));
        return SUCCESS;
    }
}
