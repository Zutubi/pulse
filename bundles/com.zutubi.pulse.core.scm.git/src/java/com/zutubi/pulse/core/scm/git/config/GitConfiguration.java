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
@Form(fieldOrder = {"repository", "branch", "trackSelectedBranch", "cloneType", "submoduleProcessing", "selectedSubmodules", "inactivityTimeoutEnabled", "inactivityTimeoutSeconds", "monitor", "customPollingInterval", "pollingInterval", "includedPaths", "excludedPaths", "quietPeriodEnabled", "quietPeriod"})
public class GitConfiguration extends PollableScmConfiguration
{
    public enum CloneType
    {
        SELECTED_BRANCH_ONLY,
        NORMAL,
        FULL_MIRROR,
    }

    public enum SubmoduleProcessing
    {
        NONE,
        UPDATE_ALL_RECURSIVELY,
        UPDATE_SELECTED,
    }

    @Required
    private String repository;
    @Required
    private String branch = "master";
    @Required
    private CloneType cloneType = CloneType.NORMAL;
    @Required @ControllingSelect(dependentFields = "selectedSubmodules", enableSet = {"UPDATE_SELECTED"})
    private SubmoduleProcessing submoduleProcessing = SubmoduleProcessing.NONE;
    private String selectedSubmodules;
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

    public CloneType getCloneType()
    {
        return cloneType;
    }

    public void setCloneType(CloneType cloneType)
    {
        this.cloneType = cloneType;
    }

    public SubmoduleProcessing getSubmoduleProcessing()
    {
        return submoduleProcessing;
    }

    public void setSubmoduleProcessing(SubmoduleProcessing submoduleProcessing)
    {
        this.submoduleProcessing = submoduleProcessing;
    }

    public String getSelectedSubmodules()
    {
        return selectedSubmodules;
    }

    public void setSelectedSubmodules(String selectedSubmodules)
    {
        this.selectedSubmodules = selectedSubmodules;
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
