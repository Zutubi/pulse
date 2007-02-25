package com.zutubi.pulse.prototype.config;

import com.zutubi.prototype.Formatter;

/**
 *
 *
 */
public class ArtifactConfigurationFormatter implements Formatter<ArtifactConfiguration>
{
    public ArtifactConfigurationFormatter()
    {
    }

    public String format(ArtifactConfiguration obj)
    {
        return obj.getName();
    }
}
