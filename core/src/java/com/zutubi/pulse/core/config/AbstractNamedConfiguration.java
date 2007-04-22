package com.zutubi.pulse.core.config;

import com.zutubi.config.annotations.ID;

/**
 * Abstract base to be extended by named configuration objects.
 */
public abstract class AbstractNamedConfiguration implements NamedConfiguration
{
    @ID
    private String name;

    public AbstractNamedConfiguration()
    {
    }

    public AbstractNamedConfiguration(String name)
    {
        this.name = name;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }
}
