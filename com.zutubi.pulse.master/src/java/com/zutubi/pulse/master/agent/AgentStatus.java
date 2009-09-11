package com.zutubi.pulse.master.agent;

import com.zutubi.util.EnumUtils;

public enum AgentStatus
{
    DISABLED(false, false, true),
    INITIAL(false, false, false),
    OFFLINE(false, false, false),
    VERSION_MISMATCH(false, false, false),
    TOKEN_MISMATCH(false, false, false),
    INVALID_MASTER(false, false, false),
    RECIPE_ASSIGNED(true, true, false),
    BUILDING(true, true, false),
    POST_RECIPE(true, true, true),
    AWAITING_PING(true, true, false),
    BUILDING_INVALID(true, true, false),
    IDLE(true, false, false);

    private boolean online;
    private boolean busy;
    private boolean ignorePings;

    AgentStatus(boolean online, boolean busy, boolean ignorePings)
    {
        this.online = online;
        this.busy = busy;
        this.ignorePings = ignorePings;
    }

    public String getPrettyString()
    {
        return EnumUtils.toPrettyString(this);
    }

    public boolean isOnline()
    {
        return online;
    }

    public boolean isBusy()
    {
        return busy;
    }

    public boolean isIgnorePings()
    {
        return ignorePings;
    }
}
