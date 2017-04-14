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

package com.zutubi.pulse.servercore.hessian;

import com.zutubi.pulse.core.hessian.HessianConfigurationExtensionManager;
import com.zutubi.pulse.core.plugins.Plugin;
import com.zutubi.pulse.core.plugins.PluginManager;

import java.security.SecureClassLoader;

/**
 * Classloader implementation that is aware of the configuration system, delegating requests
 * to load configuration instance types to the appropriate plugins.
 *
 */
public class CustomHessianClassLoader extends SecureClassLoader
{
    private HessianConfigurationExtensionManager registry;
    private PluginManager pluginManager;

    public CustomHessianClassLoader(ClassLoader parent)
    {
        super(parent);
    }

    protected Class<?> findClass(String name) throws ClassNotFoundException
    {
        try
        {
            return super.findClass(name);
        }
        catch (ClassNotFoundException e)
        {
            String contributor = registry.getContributor(name);
            if (contributor != null)
            {
                return pluginManager.getPlugin(contributor).loadClass(name);
            }
            else
            {
                // Search all plugins.  This could be slow, and although there
                // are ways to improve it for now we rely on caching of the
                // loaded classes to reduce the impact.
                for (Plugin plugin: pluginManager.getPlugins())
                {
                    try
                    {
                        return plugin.loadClass(name);
                    }
                    catch (ClassNotFoundException ex)
                    {
                        // Try next plugin
                    }
                }
            }
        }

        throw new ClassNotFoundException(name);
    }

    public void setHessianExtensionManager(HessianConfigurationExtensionManager registry)
    {
        this.registry = registry;
    }

    public void setPluginManager(PluginManager pluginManager)
    {
        this.pluginManager = pluginManager;
    }
}
