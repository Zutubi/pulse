package com.zutubi.pulse.prototype;

import com.zutubi.prototype.annotation.Form;

/**
 *
 *
 */
@Form(fieldOrder = {"name", "description", "url"})
public class GeneralConfiguration
{
    private String name;
    private String description;
    private String url;

    private NestedConfiguration nested;

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

    public NestedConfiguration getNested()
    {
        return nested;
    }

    public void setNested(NestedConfiguration nested)
    {
        this.nested = nested;
    }
}
