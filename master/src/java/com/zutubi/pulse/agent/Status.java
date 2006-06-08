package com.zutubi.pulse.agent;

// IMPORTANT: more available statuses must appear later (ordinals are
// compared).

public enum Status
{
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
