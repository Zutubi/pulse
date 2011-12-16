package com.zutubi.pulse.core.scm.hg.config;

import com.zutubi.pulse.core.scm.config.api.PollableScmConfiguration;
import com.zutubi.pulse.core.scm.hg.MercurialClient;
import com.zutubi.tove.annotations.*;
import com.zutubi.validation.annotations.Required;

/**
 * Configures integration with the Mercurial (http://mercurial.selenic.com/) SCM.
 */
@SymbolicName("zutubi.mercurialConfig")
@ConfigurationCheck("MercurialConfigurationCheckHandler")
@Form(fieldOrder = {"repository", "branch", "checkoutScheme", "inactivityTimeoutEnabled", "inactivityTimeoutSeconds", "monitor", "customPollingInterval", "pollingInterval", "includedPaths", "excludedPaths", "quietPeriodEnabled", "quietPeriod"})
public class MercurialConfiguration extends PollableScmConfiguration
{
    @Required
    private String repository;
    private String branch;
    @ControllingCheckbox(checkedFields = "inactivityTimeoutSeconds") @Wizard.Ignore
    private boolean inactivityTimeoutEnabled = false;
    @Wizard.Ignore
    private int inactivityTimeoutSeconds = 300;

    @Transient
    public String getType()
    {
        return MercurialClient.TYPE;
    }

    public String getRepository()
    {
        return repository;
    }

    public void setRepository(String repository)
    {
        this.repository = repository;
    }

    public String getBranch()
    {
        return branch;
    }

    public void setBranch(String branch)
    {
        this.branch = branch;
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
