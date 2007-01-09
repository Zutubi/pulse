package com.zutubi.pulse.plugins.update;

/**
 * A reference from a feature to an included feature.
 */
public class FeatureInclusion
{
    private String id;
    private String version;
    private String name;
    private boolean optional = false;

    public FeatureInclusion(String id, String version, String name, boolean optional)
    {
        this.id = id;
        this.version = version;
        this.name = name;
        this.optional = optional;
    }

    public String getId()
    {
        return id;
    }

    public String getVersion()
    {
        return version;
    }

    public String getName()
    {
        return name;
    }

    public boolean isOptional()
    {
        return optional;
    }
}
