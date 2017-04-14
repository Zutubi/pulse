/* Copyright 2017 Zutubi Pty Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
