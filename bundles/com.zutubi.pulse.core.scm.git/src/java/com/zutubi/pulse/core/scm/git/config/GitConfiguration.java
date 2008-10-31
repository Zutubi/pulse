package com.zutubi.pulse.core.scm.git.config;

import com.zutubi.tove.annotations.Form;
import com.zutubi.tove.annotations.SymbolicName;
import com.zutubi.tove.annotations.Transient;
import com.zutubi.pulse.core.scm.config.api.PollableScmConfiguration;
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
    @Required
    private String repository;

    @Required
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
