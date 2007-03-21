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
