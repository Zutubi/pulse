package com.zutubi.tove.config;

import com.zutubi.tove.config.api.Configuration;
import com.zutubi.tove.type.CompositeType;
import com.zutubi.tove.type.TypeException;

/**
 * Common interface for configuration type registries.  Actual registries need to be provided outside Tove, built on
 * top of a {@link com.zutubi.tove.type.TypeRegistry}.
 */
public interface ConfigurationRegistry
{
    <T extends Configuration> CompositeType registerConfigurationType(Class<T> clazz) throws TypeException;
    CompositeType getConfigurationCheckType(CompositeType type);

}
