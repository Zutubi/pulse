package com.zutubi.pulse.plugins.update;

/**
 * A dependency of a feature on either another feature or plugin.
 */
public class FeatureRequirement
{
    private boolean feature;
    private String id;
    private String version;
    private VersionMatch match = VersionMatch.COMPATIBLE;

    public FeatureRequirement(boolean feature, String id, String version, VersionMatch match)
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

    public String getVersion()
    {
        return version;
    }

    public VersionMatch getMatch()
    {
        return match;
    }
}
