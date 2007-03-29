package com.zutubi.pulse.prototype.config;

import com.zutubi.prototype.annotation.Form;
import com.zutubi.validation.annotations.Required;
import com.zutubi.validation.annotations.Name;
import com.zutubi.validation.annotations.Url;

/**
 *
 *
 */
@Form(fieldOrder = {"name", "url", "description"})
public class ProjectConfiguration
{
    @Required() @Name()
    private String name;

    @Url()
    private String url;

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
