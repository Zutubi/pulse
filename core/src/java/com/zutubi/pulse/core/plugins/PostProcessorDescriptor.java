package com.zutubi.pulse.core.plugins;

import java.util.LinkedList;
import java.util.List;

/**
 */
public class PostProcessorDescriptor
{
    private String name;
    private String displayName;
    private boolean defaultFragment;
    private List<String> templateFragments = new LinkedList<String>();

    public PostProcessorDescriptor(String name, String displayName, boolean defaultFragment)
    {
        this.name = name;
        this.displayName = displayName;
        this.defaultFragment = defaultFragment;
    }

    void addTemplateFragment(String template)
    {
        templateFragments.add(template);
    }

    public String getName()
    {
        return name;
    }

    public boolean isDefaultFragment()
    {
        return defaultFragment;
    }

    public String getDisplayName()
    {
        return displayName;
    }

    public List<String> getTemplateFragments()
    {
        return templateFragments;
    }
}
