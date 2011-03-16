package com.zutubi.pulse.core.plugins;

import com.zutubi.pulse.core.postprocessors.api.PostProcessorConfiguration;

import java.util.LinkedList;
import java.util.List;

/**
 * Describes a plugged-in post-processor.
 */
public class PostProcessorDescriptor
{
    private String name;
    private String displayName;
    private boolean contributeDefault;
    private Class<? extends PostProcessorConfiguration> clazz;
    private List<String> templateFragments = new LinkedList<String>();

    public PostProcessorDescriptor(String name, String displayName, boolean contributeDefault, Class<? extends PostProcessorConfiguration> clazz)
    {
        this.name = name;
        this.displayName = displayName;
        this.contributeDefault = contributeDefault;
        this.clazz = clazz;
    }

    void addTemplateFragment(String template)
    {
        templateFragments.add(template);
    }

    public String getName()
    {
        return name;
    }

    public boolean isContributeDefault()
    {
        return contributeDefault;
    }

    public String getDisplayName()
    {
        return displayName;
    }

    public Class<? extends PostProcessorConfiguration> getClazz()
    {
        return clazz;
    }

    public List<String> getTemplateFragments()
    {
        return templateFragments;
    }
}
