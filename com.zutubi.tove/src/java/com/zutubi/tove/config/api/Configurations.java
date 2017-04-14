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


