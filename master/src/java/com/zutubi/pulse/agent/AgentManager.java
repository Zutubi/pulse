package com.zutubi.pulse.agent;

import com.zutubi.pulse.model.AgentState;
import com.zutubi.pulse.prototype.config.agent.AgentConfiguration;
import com.zutubi.pulse.prototype.config.agent.AgentConfigurationActions;
import com.zutubi.pulse.security.SecureParameter;
import com.zutubi.pulse.security.SecureResult;
import com.zutubi.pulse.services.UpgradeStatus;

import java.util.List;

/**
 */
public interface AgentManager
{
    String GLOBAL_AGENT_NAME = "global agent template";
    String MASTER_AGENT_NAME = "master";

    @SecureResult
    List<Agent> getAllAgents();
    @SecureResult
    List<Agent> getOnlineAgents();
    @SecureResult
    Agent getAgent(long handle);
    @SecureResult
    Agent getAgent(String name);

    @SecureParameter(parameterType = AgentConfiguration.class, action = AgentConfigurationActions.ACTION_PING)
    void pingAgent(AgentConfiguration agentConfig);
    void pingAgents();

    int getAgentCount();

    @SecureParameter(parameterType = AgentConfiguration.class, action = AgentConfigurationActions.ACTION_DISABLE)
    void setAgentState(AgentConfiguration agentConfig, AgentState.EnableState state);

    void upgradeStatus(UpgradeStatus upgradeStatus);
}
