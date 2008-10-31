package com.zutubi.pulse.master.agent;

import com.zutubi.pulse.master.model.AgentState;
import com.zutubi.pulse.master.security.SecureParameter;
import com.zutubi.pulse.master.tove.config.agent.AgentConfigurationActions;

/**
 */
public interface AgentPersistentStatusManager
{
    @SecureParameter(parameterType = Agent.class, action = AgentConfigurationActions.ACTION_DISABLE)
    void setEnableState(Agent agent, AgentState.EnableState state);
}
