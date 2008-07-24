package com.zutubi.pulse.agent;

import com.zutubi.pulse.model.Slave;

/**
 */
public interface AgentPersistentStatusManager
{
    void setEnableState(Agent agent, Slave.EnableState state);
}
