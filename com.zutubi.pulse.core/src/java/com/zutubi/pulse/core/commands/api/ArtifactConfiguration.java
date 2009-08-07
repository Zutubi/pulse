package com.zutubi.pulse.core.commands.api;

import com.zutubi.tove.annotations.SymbolicName;
import com.zutubi.tove.config.api.NamedConfiguration;

/**
 * Basic interface for artifact capture configurations.
 */
@SymbolicName("zutubi.artifactConfig")
public interface ArtifactConfiguration extends NamedConfiguration
{
    /**
     * Indicates the type of artifact to create for this configuration.  This artifact type must
     * have a single-argument constructor which will accept this configuration.
     *
     * @return the type of artifact to create for this configuration
     */
    Class<? extends Artifact> artifactType();
}
