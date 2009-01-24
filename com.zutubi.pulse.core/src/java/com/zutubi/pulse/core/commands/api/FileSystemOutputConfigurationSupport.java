package com.zutubi.pulse.core.commands.api;

import com.zutubi.pulse.core.postprocessors.api.PostProcessorConfiguration;
import com.zutubi.tove.annotations.Reference;
import com.zutubi.tove.annotations.SymbolicName;

import java.util.List;

/**
 */
@SymbolicName("zutubi.fileSystemOutputConfigSupport")
public abstract class FileSystemOutputConfigurationSupport extends OutputConfigurationSupport
{
    private boolean failIfNotPresent = true;
    private boolean ignoreStale = false;
    private String type;
    @Reference
    private List<PostProcessorConfiguration> postProcessors;

    public boolean isFailIfNotPresent()
    {
        return failIfNotPresent;
    }

    public void setFailIfNotPresent(boolean failIfNotPresent)
    {
        this.failIfNotPresent = failIfNotPresent;
    }

    public boolean isIgnoreStale()
    {
        return ignoreStale;
    }

    public void setIgnoreStale(boolean ignoreStale)
    {
        this.ignoreStale = ignoreStale;
    }

    public String getType()
    {
        return type;
    }

    public void setType(String type)
    {
        this.type = type;
    }

    public List<PostProcessorConfiguration> getPostProcessors()
    {
        return postProcessors;
    }

    public void setPostProcessors(List<PostProcessorConfiguration> postProcessors)
    {
        this.postProcessors = postProcessors;
    }
}
