package com.zutubi.pulse.plugins.update;

/**
 * Constants to specify how strictly to compare versions in dependency
 * resolution.
 */
public enum VersionMatch
{
    COMPATIBLE
    {
        public boolean versionsMatch(Version installed, Version required)
        {
            return installed.getMajor() == required.getMajor() && installed.compareTo(required) >= 0;
        }
    },
    EQUIVALENT
    {
        public boolean versionsMatch(Version installed, Version required)
        {
            return installed.getMajor() == required.getMajor() && installed.getMinor() == required.getMinor() && installed.compareTo(required) >= 0;
        }
    },
    GREATER_OR_EQUAL
    {
        public boolean versionsMatch(Version installed, Version required)
        {
            return installed.compareTo(required) >= 0;
        }
    },
    PERFECT
    {
        public boolean versionsMatch(Version v1, Version v2)
        {
            return v1.equals(v2);
        }
    };

    public abstract boolean versionsMatch(Version installed, Version required);
    
}
