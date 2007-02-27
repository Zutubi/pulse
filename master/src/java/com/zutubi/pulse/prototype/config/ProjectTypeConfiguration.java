package com.zutubi.pulse.prototype.config;

import com.zutubi.prototype.annotation.TypeSelect;

/**
 *
 *
 */
public class ProjectTypeConfiguration implements ConfigurationExtension
{
    private String option;

    @TypeSelect(configurationType = ProjectTypeConfiguration.class, size = 5)
    public String getOption()
    {
        return option;
    }

    public void setOption(String option)
    {
        this.option = option;
    }
}
