package com.zutubi.tove.variables;

import com.zutubi.tove.config.api.Configuration;
import com.zutubi.tove.variables.api.VariableMap;

/**
 * Provides a context in which to resolve variables given a configuration instance.
 */
public interface ConfigurationVariableProvider
{
    /**
     * Yields a collection of variables that apply to the given config.  These may not only come 
     * directly from the config, but may include variables from its surrounding context (e.g. its
     * parent configuration).
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
     */
    <T extends Configuration> T resolveStringProperties(T config);
}
