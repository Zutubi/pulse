package com.zutubi.pulse.master.agent;

import com.zutubi.pulse.master.security.SecureParameter;
import com.zutubi.pulse.master.security.SecureResult;
import com.zutubi.pulse.master.tove.config.agent.AgentConfiguration;
import com.zutubi.pulse.master.tove.config.agent.AgentConfigurationActions;
import com.zutubi.pulse.servercore.services.UpgradeStatus;

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
