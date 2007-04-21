package com.zutubi.pulse.prototype.config;

import com.zutubi.config.annotations.ID;
import com.zutubi.config.annotations.Wizard;
import com.zutubi.pulse.prototype.record.SymbolicName;

/**
 *
 *
 */
@SymbolicName("commitConfig")
@Wizard("CommitMessageConfigurationWizard")
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
