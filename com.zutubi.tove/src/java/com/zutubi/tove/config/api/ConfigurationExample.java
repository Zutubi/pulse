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

/**
 * Holds information about an example usage of a configuration object.
 */
public class ConfigurationExample
{
    private String element;
    private Configuration configuration;

    /**
     * Creates a new example.
     *
     * @param element       name to use for the root element when rendering of
     *                      this example as XML
     * @param configuration pre-configured instance that shows typical usage of
     *                      a configuration type
     */
    public ConfigurationExample(String element, Configuration configuration)
    {
        this.element = element;
        this.configuration = configuration;
    }

    /**
     * @return name to use for the root element when rendering of this example
     *         as XML
     */
    public String getElement()
    {
        return element;
    }

    /**
     * @return pre-configured instance that shows typical usage of a
     *         configuration type
     */
    public Configuration getConfiguration()
    {
        return configuration;
    }
}
