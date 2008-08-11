package com.zutubi.pulse.agent;

public enum Status
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

                public boolean isBusy()
                {
                    return false;
                }

                public boolean ignorePings()
                {
                    return true;
                }
            },
    INITIAL
            {
                public String getPrettyString()
                {
                    return "initial";
                }

                public boolean isOnline()
                {
                    return false;
                }

                public boolean isBusy()
                {
                    return false;
                }

                public boolean ignorePings()
                {
                    return false;
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

                public boolean isBusy()
                {
                    return false;
                }

                public boolean ignorePings()
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

                public boolean isBusy()
                {
                    return false;
                }

                public boolean ignorePings()
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

                public boolean isBusy()
                {
                    return false;
                }

                public boolean ignorePings()
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

                public boolean isBusy()
                {
                    return false;
                }

                public boolean ignorePings()
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

                public boolean isBusy()
                {
                    return true;
                }

                public boolean ignorePings()
                {
                    return false;
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

                public boolean isBusy()
                {
                    return true;
                }

                public boolean ignorePings()
                {
                    return false;
                }
            },
    POST_RECIPE
            {
                public String getPrettyString()
                {
                    return "post recipe";
                }

                public boolean isOnline()
                {
                    return true;
                }

                public boolean isBusy()
                {
                    return true;
                }

                public boolean ignorePings()
                {
                    return true;
                }
            },
    AWAITING_PING
            {
                public String getPrettyString()
                {
                    return "awaiting ping";
                }

                public boolean isOnline()
                {
                    return true;
                }

                public boolean isBusy()
                {
                    return true;
                }

                public boolean ignorePings()
                {
                    return false;
                }
            },
    BUILDING_INVALID
            {
                public String getPrettyString()
                {
                    return "building invalid";
                }

                public boolean isOnline()
                {
                    return true;
                }

                public boolean isBusy()
                {
                    return true;
                }

                public boolean ignorePings()
                {
                    return false;
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

                public boolean isBusy()
                {
                    return false;
                }

                public boolean ignorePings()
                {
                    return false;
                }
            };

    public abstract String getPrettyString();
    public abstract boolean isOnline();
    public abstract boolean isBusy();
    public abstract boolean ignorePings();
    
}
