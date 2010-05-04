package com.zutubi.pulse.core.scm.git.config;

import com.zutubi.pulse.core.scm.config.api.PollableScmConfiguration;
import com.zutubi.pulse.core.scm.git.GitClient;
import com.zutubi.tove.annotations.*;
import com.zutubi.validation.annotations.Required;

/**
 * Configures integration with the git (http://www.git-scm.org/) SCM.
 */
@SymbolicName("zutubi.gitConfig")
@ConfigurationCheck("GitConfigurationCheckHandler")
@Form(fieldOrder = {"repository", "branch", "trackSelectedBranch", "checkoutScheme", "inactivityTimeoutEnabled", "inactivityTimeoutSeconds", "monitor", "customPollingInterval", "pollingInterval", "quietPeriodEnabled", "quietPeriod"})
public class GitConfiguration extends PollableScmConfiguration
{
    @Required
    private String repository;
    @Required
    private String branch = "master";
    private boolean trackSelectedBranch = false;
    @ControllingCheckbox(checkedFields = "inactivityTimeoutSeconds") @Wizard.Ignore
    private boolean inactivityTimeoutEnabled = false;
    @Wizard.Ignore
    private int inactivityTimeoutSeconds = 300;

    @Transient
    public String getType()
    {
        return GitClient.TYPE;
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

    public boolean isTrackSelectedBranch()
    {
        return trackSelectedBranch;
    }

    public void setTrackSelectedBranch(boolean trackSelectedBranch)
    {
        this.trackSelectedBranch = trackSelectedBranch;
    }

    public boolean isInactivityTimeoutEnabled()
    {
        return inactivityTimeoutEnabled;
    }

    public void setInactivityTimeoutEnabled(boolean inactivityTimeoutEnabled)
    {
        this.inactivityTimeoutEnabled = inactivityTimeoutEnabled;
    }

    public int getInactivityTimeoutSeconds()
    {
        return inactivityTimeoutSeconds;
    }

    public void setInactivityTimeoutSeconds(int inactivityTimeoutSeconds)
    {
        this.inactivityTimeoutSeconds = inactivityTimeoutSeconds;
    }
}
