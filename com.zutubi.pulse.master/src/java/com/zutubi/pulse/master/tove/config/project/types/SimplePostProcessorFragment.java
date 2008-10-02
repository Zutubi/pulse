package com.zutubi.pulse.master.tove.config.project.types;

/**
 * A fragment with all values cached.
 */
public class SimplePostProcessorFragment implements PostProcessorFragment
{
    private String name;
    private String displayName;
    private String fragment;

    public SimplePostProcessorFragment(String name, String displayName, String fragment)
    {
        this.name = name;
        this.displayName = displayName;
        this.fragment = fragment;
    }

    public String getName()
    {
        return name;
    }

    public String getDisplayName()
    {
        return displayName;
    }

    public String getFragment()
    {
        return fragment;
    }
}
