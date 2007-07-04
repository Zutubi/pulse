package com.zutubi.pulse.prototype.config.project.commit;

import com.zutubi.config.annotations.SymbolicName;

/**
 *
 *
 */
@SymbolicName("zutubi.customCommitMessageConfig")
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
