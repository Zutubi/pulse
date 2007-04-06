package com.zutubi.pulse.prototype.config;

import com.zutubi.prototype.annotation.Form;
import com.zutubi.pulse.prototype.record.SymbolicName;
import com.zutubi.validation.annotations.Required;
import com.zutubi.validation.annotations.Name;
import com.zutubi.validation.annotations.Url;

/**
 *
 *
 */
@Form(fieldOrder = {"name", "url", "description"})
@SymbolicName("internal.GeneralConfiguration")
public class GeneralConfiguration
{
    @Required @Name
    private String name;

    private String description;

    @Url
    private String url;

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public String getUrl()
    {
        return url;
    }

    public void setUrl(String url)
    {
        this.url = url;
    }
}
