package com.zutubi.pulse.prototype.config;

import com.zutubi.config.annotations.annotation.*;
import com.zutubi.validation.annotations.Url;

/**
 *
 *
 */
@Form(fieldOrder = {"name", "url", "description"})
@Wizard("com.zutubi.prototype.wizard.webwork.ConfigureProjectWizard")
@Format("ProjectConfigurationFormatter")
public class ProjectConfiguration
{
    @ID
    private String name;

    @Url
    private String url;

    @TextArea
    private String description;

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getUrl()
    {
        return url;
    }

    public void setUrl(String url)
    {
        this.url = url;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }
}
