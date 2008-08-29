package com.zutubi.pulse.core.git.config;

import com.zutubi.pulse.core.scm.config.PollableScmConfiguration;
import com.zutubi.config.annotations.Transient;

/**
 *
 *
 */
public class GitConfiguration extends PollableScmConfiguration
{
    @Transient
    public String getType()
    {
        return "git";
    }

    public String getPreviousRevision(String revision)
    {
        return null;
    }
}
