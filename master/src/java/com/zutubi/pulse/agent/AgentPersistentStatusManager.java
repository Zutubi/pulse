package com.zutubi.pulse.agent;

import com.zutubi.pulse.model.AgentState;
import com.zutubi.pulse.security.SecureParameter;
import com.zutubi.pulse.tove.config.agent.AgentConfigurationActions;

/**
 */
public interface AgentPersistentStatusManager
{
    @SecureParameter(parameterType = Agent.class, action = AgentConfigurationActions.ACTION_DISABLE)
    void setEnableState(Agent agent, AgentState.EnableState state);
}
