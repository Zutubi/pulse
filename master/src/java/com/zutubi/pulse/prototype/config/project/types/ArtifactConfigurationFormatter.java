package com.zutubi.pulse.prototype.config.project.types;

import com.zutubi.prototype.ConfigurationFormatter;

/**
 *
 *
 */
public class ArtifactConfigurationFormatter  implements ConfigurationFormatter
{
    public String getDetails(ArtifactConfiguration config)
    {
        return config.toString();
    }
}
