package com.zutubi.pulse.plugins.update;

import com.zutubi.pulse.plugins.PluginVersion;

/**
 * A dependency of a feature on either another feature or plugin.
 */
public class FeatureRequirement
{
    private boolean feature;
    private String id;
    private PluginVersion version;
    private VersionMatch match = VersionMatch.COMPATIBLE;

    public FeatureRequirement(boolean feature, String id, PluginVersion version, VersionMatch match)
    {
        this.feature = feature;
        this.id = id;
        this.version = version;
        this.match = match;
    }

    public boolean isFeature()
    {
        return feature;
    }

    public String getId()
    {
        return id;
    }

    public PluginVersion getVersion()
    {
        return version;
    }

    public VersionMatch getMatch()
    {
        return match;
    }

    public boolean satisfied(PluginVersion installed)
    {
        return version == null || match == null || match.versionsMatch(installed, version);
    }


    public String toString()
    {
        String result = (feature ? "Feature" : "Plugin") + " '" + id + "'";
        if(version != null)
        {
            result += " version " + version.toString();
            switch(match)
            {
                case COMPATIBLE:
                    result += " or compatible (same major)";
                    break;
                case EQUIVALENT:
                    result += " or equivalent (same major.minor)";
                    break;
                case GREATER_OR_EQUAL:
                    result += " or greater";
            }
        }

        return result;
    }
}
