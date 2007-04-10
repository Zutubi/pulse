package com.zutubi.pulse.prototype.config;

/**
 *
 *
 */
public class CustomCommitMessageConfiguration extends CommitMessageConfiguration
{
    private String regex;

    public String getRegex()
    {
        return regex;
    }

    public void setRegex(String regex)
    {
        this.regex = regex;
    }
}
