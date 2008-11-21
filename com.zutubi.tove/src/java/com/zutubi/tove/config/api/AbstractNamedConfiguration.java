package com.zutubi.tove.config.api;

/**
 * Abstract base to be extended by named configuration objects.
 */
public abstract class AbstractNamedConfiguration extends AbstractConfiguration implements NamedConfiguration
{
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
