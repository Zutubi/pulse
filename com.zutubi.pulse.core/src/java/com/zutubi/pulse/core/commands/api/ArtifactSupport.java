package com.zutubi.pulse.core.commands.api;

/**
 * Support base class for output capturing types.  Stores the configuration.
 */
public abstract class ArtifactSupport implements Artifact
{
    private ArtifactConfiguration config;

    /**
     * Creates a new output based on the given configuration.
     *
     * @param config configuration for this output
     */
    protected ArtifactSupport(ArtifactConfiguration config)
    {
        this.config = config;
    }

    /**
     * Returns the configuration for this output.
     *
     * @return the configuration for this output
     */
    public ArtifactConfiguration getConfig()
    {
        return config;
    }
}
