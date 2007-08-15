package com.zutubi.pulse.core.scm;

import com.zutubi.pulse.plugins.AbstractExtensionManager;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.dynamichelpers.IExtensionTracker;

/**
 * Extension manager for managing scm implementations
 * 
 */
public class ScmExtensionManager extends AbstractExtensionManager
{
    private DelegateScmClientFactory clientFactory;

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
            String clientClass = config.getAttribute("factory-class");
            String wcClass = config.getAttribute("working-copy-class");
            System.out.println(String.format("Adding SCM: %s -> (%s, %s)", name, configClass, clientClass));

            Class configClazz = loadClass(extension, configClass);
            Class clientClazz = loadClass(extension, clientClass);
            Class wcClazz = loadClass(extension, wcClass);
            if(configClazz != null && clientClazz != null)
            {
                tracker.registerObject(extension, name, IExtensionTracker.REF_WEAK);
            }

            clientFactory.register(configClazz, clientClazz);

            try
            {
                ScmConfiguration configInstance = (ScmConfiguration) configClazz.newInstance();
                WorkingCopyFactory.registerType(configInstance.getType(), wcClazz);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        catch (ScmException e)
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
}
