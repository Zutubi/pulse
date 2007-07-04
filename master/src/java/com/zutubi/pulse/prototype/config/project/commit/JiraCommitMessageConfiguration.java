package com.zutubi.pulse.prototype.config.project.commit;

import com.zutubi.config.annotations.SymbolicName;

/**
 *
 *
 */
@SymbolicName("zutubi.jiraCommitMessageConfig")
public class JiraCommitMessageConfiguration extends CommitMessageConfiguration
{
    private String url;
    private String regex;

    public String getUrl()
    {
        return url;
    }

    public void setUrl(String url)
    {
        this.url = url;
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
