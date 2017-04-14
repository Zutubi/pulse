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

package com.zutubi.tove.events;

import com.zutubi.tove.config.ConfigurationProvider;

/**
 * An event raised when the configuration system is completely available.
 * Handlers are free to trigger changes to the configuration upon receipt of
 * this event.  Note that to register configuration listeners, you should
 * handle the {@link ConfigurationEventSystemStartedEvent} instead - changes
 * may occur before your handler is called for this event.
 *
 * @see ConfigurationEventSystemStartedEvent
 */
public class ConfigurationSystemStartedEvent extends ConfigurationSystemEvent
{
    private ConfigurationProvider configurationProvider;

    public ConfigurationSystemStartedEvent(ConfigurationProvider configurationProvider)
    {
        super(configurationProvider);
        this.configurationProvider = configurationProvider;
    }

    public ConfigurationProvider getConfigurationProvider()
    {
        return configurationProvider;
    }

    public String toString()
    {
        return "Configuration System Started Event";
    }
}
