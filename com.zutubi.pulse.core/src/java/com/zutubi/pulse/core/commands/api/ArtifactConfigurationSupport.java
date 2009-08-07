package com.zutubi.pulse.core.commands.api;

import com.zutubi.tove.annotations.SymbolicName;
import com.zutubi.tove.config.api.AbstractNamedConfiguration;

/**
 * Helper base class for configuration of {@link Artifact} instances.
 */
@SymbolicName("zutubi.artifactConfigSupport")
public abstract class ArtifactConfigurationSupport extends AbstractNamedConfiguration implements ArtifactConfiguration
{
    protected ArtifactConfigurationSupport()
    {
    }

    protected ArtifactConfigurationSupport(String name)
    {
        super(name);
    }
}
