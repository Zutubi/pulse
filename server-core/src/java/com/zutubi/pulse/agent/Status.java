package com.zutubi.pulse.agent;

public enum Status
{
    DISABLED
            {
                public String getPrettyString()
                {
                    return "disabled";
                }
            },
    OFFLINE
            {
                public String getPrettyString()
                {
                    return "offline";
                }
            },
    VERSION_MISMATCH
            {
                public String getPrettyString()
                {
                    return "version mismatch";
                }
            },
    BUILDING
            {
                public String getPrettyString()
                {
                    return "building";
                }
            },
    IDLE
            {
                public String getPrettyString()
                {
                    return "idle";
                }
            };

    public abstract String getPrettyString();
}
