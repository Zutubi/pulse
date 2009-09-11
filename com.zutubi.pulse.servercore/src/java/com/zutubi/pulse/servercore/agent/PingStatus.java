package com.zutubi.pulse.servercore.agent;

import com.zutubi.util.EnumUtils;

/**
 * Basic state of a host returned by a ping.
 */
public enum PingStatus
{
    OFFLINE(false),
    VERSION_MISMATCH(false),
    TOKEN_MISMATCH(false),
    INVALID_MASTER(false),
    BUILDING(true),
    IDLE(true);

    private boolean online;

    PingStatus(boolean online)
    {
        this.online = online;
    }

    /**
     * Returns a human-readable version of this state.
     *
     * @return a pretty version of this state
     */
    public String getPrettyString()
    {
        return EnumUtils.toPrettyString(this);
    }

    /**
     * Indicates if this is an online state.
     *
     * @return true if this state implies the agent is online (able to build,
     *         although possibly busy)
     */
    public boolean isOnline()
    {
        return online;
    }
}
