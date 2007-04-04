package com.zutubi.pulse.prototype.config;

import com.zutubi.prototype.Formatter;

/**
 *
 *
 */
public class ArtifactConfigurationFormatter implements Formatter<BaseArtifactConfiguration>
{
    public ArtifactConfigurationFormatter()
    {
    }

    public String format(BaseArtifactConfiguration obj)
    {
        return obj.getName();
    }
}
