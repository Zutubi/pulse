package com.zutubi.tove.variables;

import com.zutubi.tove.config.api.Configuration;
import com.zutubi.tove.variables.api.VariableMap;

/**
 * Provides a context in which to resolve variables given a configuration instance.
 */
public interface ConfigurationVariableProvider
{
    /**
     * Yields a collection of variables source from the given config.
     *
     * @param config the configuration instance to get variables for
     * @return a map of variables for the given instance
     */
    VariableMap variablesForConfiguration(Configuration config);

    /**
     * Resolves any variables found in properties of type string in the given configuration,
     * returning a configuration with those resolved properties.
     *
     * @param config the configuration to resolve
     * @param variables variable values used for resolution
     */
    <T extends Configuration> T resolveStringProperties(T config, VariableMap variables);
}
