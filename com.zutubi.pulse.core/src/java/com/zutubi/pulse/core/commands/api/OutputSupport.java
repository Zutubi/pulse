package com.zutubi.pulse.core.commands.api;

/**
 * Support base class for output capturing types.  Stores the configuration.
 */
public abstract class OutputSupport implements Output
{
    private OutputConfiguration config;

    /**
     * Creates a new output based on the given configuration.
     *
     * @param config configuration for this output
     */
    protected OutputSupport(OutputConfiguration config)
    {
        this.config = config;
    }

    /**
     * Returns the configuration for this output.
     *
     * @return the configuration for this output
     */
    public OutputConfiguration getConfig()
    {
        return config;
    }
}
