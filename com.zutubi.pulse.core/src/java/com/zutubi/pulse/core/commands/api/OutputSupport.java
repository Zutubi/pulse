package com.zutubi.pulse.core.commands.api;

/**
 */
public abstract class OutputSupport<T extends OutputConfiguration> implements Output
{
    private T config;

    protected OutputSupport(T config)
    {
        this.config = config;
    }

    public T getConfig()
    {
        return config;
    }
}
