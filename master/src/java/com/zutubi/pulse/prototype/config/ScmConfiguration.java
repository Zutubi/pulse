package com.zutubi.pulse.prototype.config;

import com.zutubi.prototype.annotation.TypeSelect;
import com.zutubi.validation.annotations.Required;

/**
 *
 *
 */
public class ScmConfiguration implements ConfigurationExtension
{
    // should the option list be coming from this class??
    @Required()
    private String option;

    @TypeSelect(configurationType = ScmConfiguration.class, size = 5)
    public String getOption()
    {
        return option;
    }

    public void setOption(String option)
    {
        this.option = option;
    }
}
