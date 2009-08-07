package com.zutubi.pulse.core.commands;

import com.zutubi.pulse.core.commands.api.Artifact;
import com.zutubi.pulse.core.commands.api.ArtifactConfiguration;

/**
 * Factory for creating artifacts from configuration.
 */
public interface ArtifactFactory
{
    /**
     * Create a new artifact from the given configuration.  The configuration
     * identifies the type of artifact to create, and that type should have a
     * single-parameter constructor which will accept the configuration as an
     * argument.
     *
     * @param configuration configuration used to build the artifact
     * @return the created artifact
     * @throws com.zutubi.pulse.core.engine.api.BuildException on any error
     */
    Artifact create(ArtifactConfiguration configuration);
}