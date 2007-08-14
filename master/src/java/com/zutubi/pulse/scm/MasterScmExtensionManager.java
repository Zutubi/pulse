package com.zutubi.pulse.scm;

import com.zutubi.pulse.plugins.AbstractExtensionManager;
import com.zutubi.pulse.core.scm.DelegateScmClientFactory;
import com.zutubi.pulse.core.scm.ScmException;
import com.zutubi.pulse.core.scm.config.ScmConfiguration;
import com.zutubi.prototype.config.ConfigurationRegistry;
import com.zutubi.prototype.type.TypeException;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.dynamichelpers.IExtensionTracker;

/**
 *
 *
 */
public class MasterScmExtensionManager extends AbstractExtensionManager
{
    private DelegateScmClientFactory clientFactory;
    private ConfigurationRegistry configurationRegistry;

    protected String getExtensionPointId()
    {
        return "com.zutubi.pulse.core.scms";
    }

    protected void handleConfigurationElement(IExtension extension, IExtensionTracker tracker, IConfigurationElement config)
    {
        // check configuration.

        try
        {
            String name = config.getAttribute("name");
            String configClass = config.getAttribute("config-class");
            String factoryClass = config.getAttribute("factory-class");
            System.out.println(String.format("Adding SCM: %s -> (%s, %s)", name, configClass, factoryClass));

            Class configClazz = loadClass(extension, configClass);
            Class factoryClazz = loadClass(extension, factoryClass);
            if(configClazz != null && factoryClazz != null)
            {
                tracker.registerObject(extension, name, IExtensionTracker.REF_WEAK);
            }

            clientFactory.register(configClazz, factoryClazz);

            configurationRegistry.registerExtension(ScmConfiguration.class, configClazz);
        }
        catch (ScmException e)
        {
            e.printStackTrace();
        }
        catch (TypeException e)
        {
            e.printStackTrace();
        }
    }

    public void removeExtension(IExtension extension, Object[] objects)
    {
        for (Object o : objects)
        {
        }
    }

    public void setScmClientFactory(DelegateScmClientFactory clientFactory)
    {
        this.clientFactory = clientFactory;
    }

    public void setConfigurationRegistry(ConfigurationRegistry configurationRegistry)
    {
        this.configurationRegistry = configurationRegistry;
    }
}
