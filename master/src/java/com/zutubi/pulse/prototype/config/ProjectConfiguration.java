package com.zutubi.pulse.prototype.config;

import com.zutubi.prototype.annotation.Form;
import com.zutubi.prototype.annotation.Format;
import com.zutubi.prototype.annotation.TextArea;
import com.zutubi.prototype.annotation.Wizard;
import com.zutubi.prototype.wizard.webwork.ConfigureProjectWizard;
import com.zutubi.validation.annotations.Name;
import com.zutubi.validation.annotations.Required;
import com.zutubi.validation.annotations.Url;

/**
 *
 *
 */
@Form(fieldOrder = {"name", "url", "description"})
@Wizard(ConfigureProjectWizard.class)
@Format(ProjectConfigurationFormatter.class)
public class ProjectConfiguration
{
    @Required @Name
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
