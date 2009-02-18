package com.zutubi.pulse.core.tove.config;

import com.zutubi.tove.config.api.Configuration;
import com.zutubi.tove.type.CompositeType;
import com.zutubi.tove.type.TypeException;

/**
 * Common interface for Pulse configuration type registries.  Abstracts
 * differences between registration in different components.
 */
public interface ConfigurationRegistry
{
    <T extends Configuration> CompositeType registerConfigurationType(Class<T> clazz) throws TypeException;
}
