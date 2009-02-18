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
