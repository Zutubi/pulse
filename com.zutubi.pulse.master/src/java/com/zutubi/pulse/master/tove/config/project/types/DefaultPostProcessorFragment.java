package com.zutubi.pulse.master.tove.config.project.types;

/**
 * A default fragment consists of a single XML tag for the processor with no
 * additional customisation.
 */
public class DefaultPostProcessorFragment implements PostProcessorFragment
{
    private String name;
    private String displayName;

    public DefaultPostProcessorFragment(String name, String displayName)
    {
        this.name = name;
        this.displayName = displayName;
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
        return String.format("<%s.pp name=\"%s\"/>", name, name);
    }
}
