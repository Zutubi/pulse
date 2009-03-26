package com.zutubi.pulse.core.commands.api;

/**
 * Support class to make implementing the Command interface simpler for the
 * simple cases.
 */
public abstract class CommandSupport implements Command
{
    private CommandConfigurationSupport config;

    /**
     * Constructor that stores the configuration for later access via
     * {@link #getConfig()}.
     *
     * @param config the configuration for this command
     */
    protected CommandSupport(CommandConfigurationSupport config)
    {
        this.config = config;
    }

    /**
     * Returns the configuration for this command.
     *
     * @return this command's configuration
     */
    public CommandConfigurationSupport getConfig()
    {
        return config;
    }

    public void terminate()
    {
    }
}
