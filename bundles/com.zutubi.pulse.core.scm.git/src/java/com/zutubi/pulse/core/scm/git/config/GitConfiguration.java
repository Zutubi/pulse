package com.zutubi.pulse.core.scm.git.config;

import com.zutubi.pulse.core.scm.config.PollableScmConfiguration;
import com.zutubi.pulse.core.scm.config.ScmConfiguration;
import com.zutubi.config.annotations.Transient;
import com.zutubi.config.annotations.SymbolicName;

/**
 *
 *
 */
@SymbolicName("zutubi.gitConfig")
public class GitConfiguration extends ScmConfiguration
{
    private String repository;

    private String branch;

    @Transient
    public String getType()
    {
        return "git";
    }

    public String getPreviousRevision(String revision)
    {
        // awkward.  The previous revision is in fact revision^.  However, this is
        // only one representation fo the previous revision, and likely not the same
        // as what we are expecting..
        return null;
    }

    public String getRepository()
    {
        return repository;
    }

    public void setRepository(String repository)
    {
        this.repository = repository;
    }

    public void setBranch(String branch)
    {
        this.branch = branch;
    }

    public String getBranch()
    {
        return branch;
    }
}
