package com.zutubi.pulse.core.commands.api;

/**
 * An artifact that "captures" a link to an external resource, for presentation
 * along other outputs in the build result.
 */
public class LinkArtifact implements Artifact
{
    private LinkArtifactConfiguration config;

    /**
     * Constructor that stores the configuration.
     *
     * @param config configuration for this artifact
     */
    public LinkArtifact(LinkArtifactConfiguration config)
    {
        this.config = config;
    }

    public void capture(CommandContext context)
    {
        context.registerLink(config.getName(), config.getUrl());
    }
}
