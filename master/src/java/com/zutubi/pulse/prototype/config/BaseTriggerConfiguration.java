package com.zutubi.pulse.prototype.config;

import com.zutubi.pulse.form.persist.annotation.Id;
import com.zutubi.validation.annotations.Required;
import com.zutubi.validation.annotations.Name;

/**
 */
public class BaseTriggerConfiguration
{
    @Required @Name
    private String name;

    @Id
    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }
}
