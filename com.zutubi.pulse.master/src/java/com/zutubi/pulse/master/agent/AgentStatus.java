package com.zutubi.pulse.master.agent;

import com.zutubi.util.EnumUtils;

/**
 * Enumerates possible states for an agent.  This includes whether the agent is
 * online and if it is available.
 */
public enum AgentStatus
{
    /**
     * The agent has been explcitly disabled, and is not in use.
     */
    DISABLED(false, false, true),
    /**
     * The agent is enabled but has not yet been pinged to determine its status.
     */
    INITIAL(false, false, false),
    /**
     * The agent is enabled but uncontactable.
     */
    OFFLINE(false, false, false),
    /**
     * The agent build number does not match the master build number.
     */
    VERSION_MISMATCH(false, false, false),
    /**
     * The agent's security token does not match the master's token.
     */
    TOKEN_MISMATCH(false, false, false),
    /**
     * The agent is contactable, but cannot itself contact the master.
     */
    INVALID_MASTER(false, false, false),
    /**
     * A recipe has been assigned to the agent, but it has not yet commenced.
     */
    RECIPE_ASSIGNED(true, true, false),
    /**
     * The agent is currently executing a recipe.
     */
    BUILDING(true, true, false),
    /**
     * The agent has completed a recipe and is running post-recipe tasks.
     */
    POST_RECIPE(true, true, true),
    /**
     * The master is waiting for the first successful ping of the agent after a recipe.
     */
    AWAITING_PING(true, true, false),
    /**
     * The agent has indicated it is building a recipe that the master is not expected.
     */
    BUILDING_INVALID(true, true, false),
    /**
     * The agent is online and not building anything.
     */
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

    /**
     * Returns a human-readble version of this constant.
     *
     * @return a pretty version of this constant
     */
    public String getPrettyString()
    {
        return EnumUtils.toPrettyString(this);
    }

    /**
     * Indicates if this is an online state.
     *
     * @return true iff agents in this state are online (contactable via pings)
     */
    public boolean isOnline()
    {
        return online;
    }

    /**
     * Indicates if this is a busy state.
     *
     * @return true iff agents in this state are busy running a recipe or related tasks
     */
    public boolean isBusy()
    {
        return busy;
    }

    /**
     * Indicates if pings are ignored in this state.
     *
     * @return true iff ping results are ignored by agents in this state
     */
    public boolean isIgnorePings()
    {
        return ignorePings;
    }
}
