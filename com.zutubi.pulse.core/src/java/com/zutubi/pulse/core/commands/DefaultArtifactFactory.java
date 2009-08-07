package com.zutubi.pulse.core.commands;

import com.zutubi.pulse.core.ConfiguredInstanceFactory;
import com.zutubi.pulse.core.commands.api.Artifact;
import com.zutubi.pulse.core.commands.api.ArtifactConfiguration;

/**
 * Default implementation of {@link ArtifactFactory}, which uses the object
 * factory to build artifacts.
 */
public class DefaultArtifactFactory extends ConfiguredInstanceFactory<Artifact, ArtifactConfiguration> implements ArtifactFactory
{
    protected Class<? extends Artifact> getType(ArtifactConfiguration configuration)
    {
        return configuration.artifactType();
    }
}