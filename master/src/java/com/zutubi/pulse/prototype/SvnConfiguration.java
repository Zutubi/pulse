package com.zutubi.pulse.prototype;

import com.zutubi.pulse.form.descriptor.annotation.Summary;

/**
 */
@Summary(fields = {"name", "url"})
public class SvnConfiguration extends BaseScmConfiguration
{
    private String url;

    private String name;

    private String password;

    public SvnConfiguration()
    {
    }

    public SvnConfiguration(String url, String name, String password)
    {
        this.url = url;
        this.name = name;
        this.password = password;
    }

    public String getUrl()
    {
        return url;
    }

    public void setUrl(String url)
    {
        this.url = url;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getPassword()
    {
        return password;
    }

    public void setPassword(String password)
    {
        this.password = password;
    }
}
