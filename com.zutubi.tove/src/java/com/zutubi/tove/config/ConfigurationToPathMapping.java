package com.zutubi.tove.config;

import com.zutubi.tove.config.api.Configuration;
import com.zutubi.util.Mapping;

/**
 * Maps from configuration instances to their config paths.
 */
public class ConfigurationToPathMapping implements Mapping<Configuration, String>
{
    public String map(Configuration configuration)
    {
        return configuration.getConfigurationPath();
    }
}
