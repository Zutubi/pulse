package com.zutubi.pulse.core.commands.api;

import com.zutubi.tove.annotations.SymbolicName;
import com.zutubi.tove.config.api.AbstractNamedConfiguration;

/**
 * Helper base class for configuration of {@link Artifact} instances.
 */
@SymbolicName("zutubi.artifactConfigSupport")
public abstract class ArtifactConfigurationSupport extends AbstractNamedConfiguration implements ArtifactConfiguration
{
    private boolean featured;

    protected ArtifactConfigurationSupport()
    {
    }

    protected ArtifactConfigurationSupport(String name)
    {
        super(name);
    }

    public boolean isFeatured()
    {
        return featured;
    }

    public void setFeatured(boolean featured)
    {
        this.featured = featured;
    }
}
