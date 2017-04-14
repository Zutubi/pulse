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

package com.zutubi.pulse.core.plugins;

/**
 * Stores information about a dependency requirement of a plugin on another
 * plugin.
 */
public class PluginDependency
{
    private String id;
    private PluginVersion version;
    private Plugin supplier;

    public PluginDependency(String id, PluginVersion version, Plugin supplier)
    {
        this.id = id;
        this.version = version;
        this.supplier = supplier;
    }

    public String getId()
    {
        return id;
    }

    public PluginVersion getVersion()
    {
        return version;
    }

    /**
     * @return the plugin that is supplying this dependency, or null if there
     * is no supplier.  Note that the supplier may not actually be loaded: in
     * which case the dependent plugin may have failed to load.
     */
    public Plugin getSupplier()
    {
        return supplier;
    }
}
