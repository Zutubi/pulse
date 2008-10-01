package com.zutubi.pulse.hessian;

import com.zutubi.pulse.plugins.PluginManager;
import com.zutubi.pulse.core.hessian.HessianConfigurationExtensionManager;

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

    public Class<?> loadClass(String name) throws ClassNotFoundException
    {
        String contributor = registry.getContributor(name);
        if (contributor != null)
        {
            return pluginManager.getPlugin(contributor).loadClass(name);
        }
        return super.loadClass(name);
    }

    protected Class<?> findClass(String name) throws ClassNotFoundException
    {
        String contributor = registry.getContributor(name);
        if (contributor != null)
        {
            return pluginManager.getPlugin(contributor).loadClass(name);
        }
        return super.findClass(name);
    }

    public void setRegistry(HessianConfigurationExtensionManager registry)
    {
        this.registry = registry;
    }

    public void setPluginManager(PluginManager pluginManager)
    {
        this.pluginManager = pluginManager;
    }
}
