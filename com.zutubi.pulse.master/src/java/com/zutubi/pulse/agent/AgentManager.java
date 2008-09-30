package com.zutubi.pulse.agent;

import com.zutubi.pulse.security.SecureParameter;
import com.zutubi.pulse.security.SecureResult;
import com.zutubi.pulse.services.UpgradeStatus;
import com.zutubi.pulse.tove.config.agent.AgentConfiguration;
import com.zutubi.pulse.tove.config.agent.AgentConfigurationActions;

import java.util.List;

/**
 */
public interface AgentManager extends AgentPersistentStatusManager
{
    String GLOBAL_AGENT_NAME = "global agent template";
    String MASTER_AGENT_NAME = "master";

    @SecureResult
    List<Agent> getAllAgents();
    @SecureResult
    List<Agent> getOnlineAgents();
    @SecureResult
    List<Agent> getAvailableAgents();
    @SecureResult
    Agent getAgent(long handle);
    @SecureResult
    Agent getAgent(AgentConfiguration agent);
    @SecureResult
    Agent getAgent(String name);

    @SecureParameter(parameterType = AgentConfiguration.class, action = AgentConfigurationActions.ACTION_PING)
    void pingAgent(AgentConfiguration agentConfig);
    void pingAgents();

    int getAgentCount();

    void upgradeStatus(UpgradeStatus upgradeStatus);
}
