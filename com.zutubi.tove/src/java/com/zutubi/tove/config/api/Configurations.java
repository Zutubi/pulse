package com.zutubi.tove.config.api;

import com.google.common.base.Function;

/**
 * Static utilities for working with Configurations.
 */
public final class Configurations
{
    // Do not instantiate
    private Configurations() {}

    /**
     * @return a function that converts a {@link NamedConfiguration} to its name.
     */
    @SuppressWarnings("unchecked")
    public static <T extends NamedConfiguration> Function<T, String> toConfigurationName()
    {
        return (Function<T, String>) ToConfigurationNameFunction.INSTANCE;
    }

    /**
     * A mapping that maps a named configuration instance to its name.
     */
    private enum ToConfigurationNameFunction implements Function<NamedConfiguration, String>
    {
        INSTANCE;
        
        public String apply(NamedConfiguration config)
        {
            return config.getName();
        }

        public String toString()
        {
            return "toConfiguratioName";
        }
    }
}


