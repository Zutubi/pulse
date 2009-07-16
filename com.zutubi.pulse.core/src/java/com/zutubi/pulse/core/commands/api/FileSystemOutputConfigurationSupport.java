package com.zutubi.pulse.core.commands.api;

import com.zutubi.pulse.core.engine.api.Addable;
import com.zutubi.pulse.core.postprocessors.api.PostProcessorConfiguration;
import com.zutubi.tove.annotations.ControllingCheckbox;
import com.zutubi.tove.annotations.Reference;
import com.zutubi.tove.annotations.SymbolicName;
import com.zutubi.tove.annotations.Wizard;
import com.zutubi.validation.annotations.ValidRegex;

import java.util.LinkedList;
import java.util.List;

/**
 * Helper base for configuring captures that capture files from the local file
 * system.
 *
 * @see com.zutubi.pulse.core.commands.api.FileSystemOutputSupport
 */
@SymbolicName("zutubi.fileSystemOutputConfigSupport")
public abstract class FileSystemOutputConfigurationSupport extends OutputConfigurationSupport
{
    @Wizard.Ignore
    private boolean failIfNotPresent = true;
    @Wizard.Ignore
    private boolean ignoreStale = false;
    @Wizard.Ignore
    private String type;
    @Reference @Addable(value = "process", attribute = "processor")
    private List<PostProcessorConfiguration> postProcessors = new LinkedList<PostProcessorConfiguration>();

    /**
     * Indicates whether or not this file system artifact should be
     * published to the internal artifact repository.
     */
    @Wizard.Ignore
    @ControllingCheckbox(dependentFields = {"artifactPattern"})
    private boolean publish = false;

    /**
     * The default artifact pattern uses the files name for the
     * artifact name, and the files extension for the artifacts
     * extension.
     */
    @ValidRegex
    private String artifactPattern = "(.+)\\.(.+)";

    protected FileSystemOutputConfigurationSupport()
    {
    }

    protected FileSystemOutputConfigurationSupport(String name)
    {
        super(name);
    }

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

    public boolean isPublish()
    {
        return publish;
    }

    public void setPublish(boolean publish)
    {
        this.publish = publish;
    }

    public String getArtifactPattern()
    {
        return artifactPattern;
    }

    public void setArtifactPattern(String artifactPattern)
    {
        this.artifactPattern = artifactPattern;
    }

    public List<PostProcessorConfiguration> getPostProcessors()
    {
        return postProcessors;
    }

    public void setPostProcessors(List<PostProcessorConfiguration> postProcessors)
    {
        this.postProcessors = postProcessors;
    }

    public void addPostProcessor(PostProcessorConfiguration postProcessor)
    {
        postProcessors.add(postProcessor);
    }
}
