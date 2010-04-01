package com.zutubi.tove.config.api;

import com.zutubi.util.Mapping;

/**
 * A mapping that maps a named configuration instance to its name.
 */
public class ToConfigurationNameMapping<T extends NamedConfiguration> implements Mapping<T, String>
{
    public String map(T config)
    {
        return config.getName();
    }
}
