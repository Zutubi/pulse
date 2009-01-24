package com.zutubi.pulse.core.commands.api;

/**
 */
public class LinkOutput implements Output
{
    private LinkOutputConfiguration config;

    public LinkOutput(LinkOutputConfiguration config)
    {
        this.config = config;
    }

    public void capture(CommandContext context)
    {
        context.registerLink(config.getName(), config.getUrl());
    }
}
