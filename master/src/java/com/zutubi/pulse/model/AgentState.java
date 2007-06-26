package com.zutubi.pulse.model;

import com.zutubi.pulse.core.model.Entity;

import java.util.LinkedList;
import java.util.List;

/**
 * Represents a slave server that builds may be farmed out to.
 */
public class AgentState extends Entity
{
    /**
     * Persistent slave states.  The minimum we need to remember even across
     * restarts to ensure disabled slaves stay that way and failed (or
     * interrupted) upgrades are not retried without user intervention.
     */
    public enum EnableState
    {
        ENABLED,
        DISABLED,
        UPGRADING,
        FAILED_UPGRADE
    }

    private EnableState enableState = EnableState.ENABLED;

    public AgentState()
    {

    }

    public boolean isDisabled()
    {
        return enableState == EnableState.DISABLED;
    }

    public boolean isEnabled()
    {
        return enableState == EnableState.ENABLED;
    }

    public EnableState getEnableState()
    {
        return enableState;
    }

    public void setEnableState(EnableState enableState)
    {
        this.enableState = enableState;
    }

    private String getEnableStateName()
    {
        return enableState.toString();
    }

    private void setEnableStateName(String name)
    {
        this.enableState = EnableState.valueOf(name);
    }
}
