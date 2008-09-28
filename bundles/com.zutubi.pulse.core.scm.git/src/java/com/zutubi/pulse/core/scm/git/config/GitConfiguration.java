package com.zutubi.pulse.core.scm.git.config;

import com.zutubi.config.annotations.Form;
import com.zutubi.config.annotations.SymbolicName;
import com.zutubi.config.annotations.Transient;
import com.zutubi.pulse.core.scm.config.PollableScmConfiguration;
import com.zutubi.validation.annotations.Required;

/**
 *
 *
 */
@SymbolicName("zutubi.gitConfig")
//@ConfigurationCheck("GitConfigurationCheckHandler")
@Form(fieldOrder = {"repository", "branch", "checkoutScheme", "monitor", "customPollingInterval", "pollingInterval", "quietPeriodEnabled", "quietPeriod"})
public class GitConfiguration extends PollableScmConfiguration
{
    private String repository;

    private String branch = "master";

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

    @Required
    public String getRepository()
    {
        return repository;
    }

    public void setRepository(String repository)
    {
        this.repository = repository;
    }

    @Required
    public void setBranch(String branch)
    {
        this.branch = branch;
    }

    public String getBranch()
    {
        return branch;
    }
}
