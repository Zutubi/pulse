package com.zutubi.pulse.tove.config.project.types;

import com.zutubi.tove.ConfigurationFormatter;

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
