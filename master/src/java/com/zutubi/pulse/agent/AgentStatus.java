package com.zutubi.pulse.agent;

/**
 */
public enum AgentStatus
{
    DISABLED
            {
                public String getPrettyString()
                {
                    return "disabled";
                }

                public boolean isOnline()
                {
                    return false;
                }
            },
    DISABLING
            {
                public String getPrettyString()
                {
                    return "disabling";
                }

                public boolean isOnline()
                {
                    return true;
                }
            },
    OFFLINE
            {
                public String getPrettyString()
                {
                    return "offline";
                }

                public boolean isOnline()
                {
                    return false;
                }
            },
    VERSION_MISMATCH
            {
                public String getPrettyString()
                {
                    return "version mismatch";
                }

                public boolean isOnline()
                {
                    return false;
                }
            },
    TOKEN_MISMATCH
            {
                public String getPrettyString()
                {
                    return "token mismatch";
                }

                public boolean isOnline()
                {
                    return false;
                }
            },
    INVALID_MASTER
            {
                public String getPrettyString()
                {
                    return "invalid master";
                }

                public boolean isOnline()
                {
                    return false;
                }
            },
    RECIPE_DISPATCHED
            {
                public String getPrettyString()
                {
                    return "recipe dispatched";
                }

                public boolean isOnline()
                {
                    return true;
                }
            },
    BUILDING
            {
                public String getPrettyString()
                {
                    return "building";
                }

                public boolean isOnline()
                {
                    return true;
                }
            },
    IDLE
            {
                public String getPrettyString()
                {
                    return "idle";
                }

                public boolean isOnline()
                {
                    return true;
                }
            };

    public abstract String getPrettyString();
    public abstract boolean isOnline();    
}
