package com.zutubi.pulse.prototype.config;

import com.zutubi.prototype.annotation.TypeSelect;
import com.zutubi.prototype.annotation.Wizard;
import com.zutubi.prototype.annotation.ID;
import com.zutubi.prototype.wizard.webwork.ConfigureProjectWizard;
import com.zutubi.pulse.prototype.record.SymbolicName;

/**
 *
 *
 */
@SymbolicName("commitConfig")
@Wizard(CommitMessageConfigurationWizard.class)
public class CommitMessageConfiguration
{
    @ID
    private String name;

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }
}
