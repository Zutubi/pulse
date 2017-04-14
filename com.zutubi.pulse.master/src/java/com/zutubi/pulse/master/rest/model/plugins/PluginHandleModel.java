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

package com.zutubi.pulse.master.rest.model.plugins;

import com.zutubi.pulse.core.plugins.Plugin;
import com.zutubi.pulse.core.plugins.PluginDependency;
import com.zutubi.pulse.core.plugins.PluginVersion;

/**
 * Models a reference to another a plugin.
 */
public class PluginHandleModel
{
    private String id;
    private PluginVersion version;
    private String name;
    private boolean available;

    public PluginHandleModel(PluginDependency pluginDependency)
    {
        id = pluginDependency.getId();
        version = pluginDependency.getVersion();
        available = pluginDependency.getSupplier() != null;
        if (available)
        {
            name = pluginDependency.getSupplier().getName();
        }
    }

    public PluginHandleModel(Plugin plugin)
    {
        id = plugin.getId();
        name = plugin.getName();
        version = plugin.getVersion();
        available = true;
    }

    public String getId()
    {
        return id;
    }

    public String getVersion()
    {
        return version.toString();
    }

    public String getName()
    {
        return name;
    }

    public boolean isAvailable()
    {
        return available;
    }
}
