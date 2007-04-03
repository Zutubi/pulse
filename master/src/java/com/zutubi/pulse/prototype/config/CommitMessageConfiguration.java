package com.zutubi.pulse.prototype.config;

import com.zutubi.prototype.annotation.TypeSelect;
import com.zutubi.prototype.annotation.Wizard;
import com.zutubi.prototype.wizard.webwork.ConfigureProjectWizard;
import com.zutubi.pulse.prototype.record.SymbolicName;

/**
 *
 *
 */
@SymbolicName("commitConfig")
@Wizard(CommitMessageConfigurationWizard.class)
public class CommitMessageConfiguration implements ConfigurationExtension
{
    private String option;

    @TypeSelect(configurationType = CommitMessageConfiguration.class, size = 5)
    public String getOption()
    {
        return option;
    }

    public void setOption(String option)
    {
        this.option = option;
    }
}
