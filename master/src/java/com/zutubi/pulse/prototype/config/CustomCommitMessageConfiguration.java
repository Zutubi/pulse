package com.zutubi.pulse.prototype.config;

import com.zutubi.prototype.annotation.ID;

/**
 *
 *
 */
public class CustomCommitMessageConfiguration
{
    private String name;
    private String regex;

    @ID()
    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getRegex()
    {
        return regex;
    }

    public void setRegex(String regex)
    {
        this.regex = regex;
    }
}
