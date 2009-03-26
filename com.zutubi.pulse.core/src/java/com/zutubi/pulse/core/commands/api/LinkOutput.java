package com.zutubi.pulse.core.commands.api;

/**
 * An output that "captures" a link to an external resource, for presentation
 * along other outputs in the build result.
 */
public class LinkOutput implements Output
{
    private LinkOutputConfiguration config;

    /**
     * Constructor that stores the configuration.
     *
     * @param config configuration for this output
     */
    public LinkOutput(LinkOutputConfiguration config)
    {
        this.config = config;
    }

    public void capture(CommandContext context)
    {
        context.registerLink(config.getName(), config.getUrl());
    }
}
