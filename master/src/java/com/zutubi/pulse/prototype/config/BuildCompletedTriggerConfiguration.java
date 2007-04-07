package com.zutubi.pulse.prototype.config;

import com.zutubi.pulse.form.descriptor.annotation.Summary;
import com.zutubi.prototype.annotation.Form;
import com.zutubi.prototype.annotation.ConfigurationCheck;
import com.zutubi.prototype.annotation.Reference;

/**
 */
@Form(fieldOrder = { "name", "project"})
public class BuildCompletedTriggerConfiguration extends BaseTriggerConfiguration
{
    private ProjectConfiguration project;

    public BuildCompletedTriggerConfiguration()
    {
    }

    @Reference(optionProvider = DefaultReferenceOptionProvider.class)
    public ProjectConfiguration getProject()
    {
        return project;
    }

    public void setProject(ProjectConfiguration project)
    {
        this.project = project;
    }
}
