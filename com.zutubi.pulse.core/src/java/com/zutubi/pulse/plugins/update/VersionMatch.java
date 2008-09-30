package com.zutubi.pulse.plugins.update;

import com.zutubi.pulse.plugins.PluginVersion;

/**
 * Constants to specify how strictly to compare versions in dependency
 * resolution.
 */
public enum VersionMatch
{
    COMPATIBLE
    {
        public boolean versionsMatch(PluginVersion installed, PluginVersion required)
        {
            return installed.getMajor() == required.getMajor() && installed.compareTo(required) >= 0;
        }
    },
    EQUIVALENT
    {
        public boolean versionsMatch(PluginVersion installed, PluginVersion required)
        {
            return installed.getMajor() == required.getMajor() && installed.getMinor() == required.getMinor() && installed.compareTo(required) >= 0;
        }
    },
    GREATER_OR_EQUAL
    {
        public boolean versionsMatch(PluginVersion installed, PluginVersion required)
        {
            return installed.compareTo(required) >= 0;
        }
    },
    PERFECT
    {
        public boolean versionsMatch(PluginVersion v1, PluginVersion v2)
        {
            return v1.equals(v2);
        }
    };

    public abstract boolean versionsMatch(PluginVersion installed, PluginVersion required);
    
}
