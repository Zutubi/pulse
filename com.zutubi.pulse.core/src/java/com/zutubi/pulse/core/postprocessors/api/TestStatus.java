package com.zutubi.pulse.core.postprocessors.api;

/**
 */
public enum TestStatus
{
    PASS
    {
        public boolean isBroken()
        {
            return false;
        }
    },
    FAILURE
    {
        public boolean isBroken()
        {
            return true;
        }
    },
    ERROR
    {
        public boolean isBroken()
        {
            return true;
        }
    };

    public abstract boolean isBroken();
}
