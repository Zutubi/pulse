package com.zutubi.pulse.core.commands.api;

/**
 * Support base class for output capturing types.  Stores the configuration.
 */
public abstract class OutputSupport<T extends OutputConfiguration> implements Output
{
    private T config;

    /**
     * Creates a new output based on the given configuration.
     *
     * @param config configuration for this output
     */
    protected OutputSupport(T config)
    {
        this.config = config;
    }

    /**
     * Returns the configuration for this output.
     *
     * @return the configuration for this output
     */
    public T getConfig()
    {
        return config;
    }
}
