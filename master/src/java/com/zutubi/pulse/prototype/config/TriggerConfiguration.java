package com.zutubi.pulse.prototype.config;

import com.zutubi.validation.annotations.Required;
import com.zutubi.prototype.annotation.TypeSelect;

/**
 */
public class TriggerConfiguration implements ConfigurationExtension
{
    // should the option list be coming from this class??
    @Required()
    private String option;

    @TypeSelect(configurationType = TriggerConfiguration.class)
    public String getOption()
    {
        return option;
    }

    public void setOption(String option)
    {
        this.option = option;
    }
}
