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
 * An event raised when the configuration system is intialised to the point
 * where configuration listeners can be registered.  This is the time to
 * register listeners that must know of all changes (i.e. no changes should
 * happen prior to this event).  Changes <strong>must not</strong> be written
 * to the configuration system before this event is raised; and indeed not
 * during handlers for this event (because other handlers have not had a
 * chance to run).  Changes must wait for the
 * {@link ConfigurationSystemStartedEvent} to fire first.
 *
 * @see ConfigurationSystemStartedEvent
 */
public class ConfigurationEventSystemStartedEvent extends ConfigurationSystemEvent
{
    private ConfigurationProvider configurationProvider;

    public ConfigurationEventSystemStartedEvent(ConfigurationProvider configurationProvider)
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
        return "Configuration Event System Started Event";
    }
}
