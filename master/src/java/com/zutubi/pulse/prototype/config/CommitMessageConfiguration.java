package com.zutubi.pulse.prototype.config;

import com.zutubi.prototype.annotation.TypeSelect;
import com.zutubi.pulse.prototype.record.SymbolicName;

/**
 *
 *
 */
@SymbolicName("commitConfig")
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
