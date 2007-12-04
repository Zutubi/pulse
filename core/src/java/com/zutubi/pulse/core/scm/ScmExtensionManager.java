package com.zutubi.pulse.core.scm;

import com.zutubi.pulse.core.scm.config.ScmConfiguration;
import com.zutubi.pulse.plugins.AbstractExtensionManager;
import com.zutubi.util.logging.Logger;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.dynamichelpers.IExtensionTracker;

/**
 * Extension manager for managing scm implementations
 */
public class ScmExtensionManager extends AbstractExtensionManager
{
    private static final Logger LOG = Logger.getLogger(ScmExtensionManager.class);

    private DelegateScmClientFactory clientFactory;

    protected String getExtensionPointId()
    {
        return "com.zutubi.pulse.core.scms";
    }

    protected void handleConfigurationElement(IExtension extension, IExtensionTracker tracker, IConfigurationElement config)
    {
        // check configuration.
        String name = config.getAttribute("name");
        String configClassName = null;
        Class configClazz = null;

        for (IConfigurationElement configElement : getConfigElements(extension))
        {
            String candidateClassName = configElement.getAttribute("class");
            Class candidateClazz = loadClass(extension, candidateClassName);
            try
            {
                ScmConfiguration configInstance = (ScmConfiguration) candidateClazz.newInstance();
                if (configInstance.getType().equals(name))
                {
                    configClassName = candidateClassName;
                    configClazz = candidateClazz;
                    break;
                }
            }
            catch (Exception e)
            {
                LOG.warning(e);
            }
        }

        if (configClassName == null)
        {
            LOG.severe("No matching configuration class found for SCM name '" + name + "': ensure that the configuration class is registered and returns '" + name + "' from getType()");
        }
        else
        {
            String factoryClassName = config.getAttribute("factory-class");
            String wcClassName = config.getAttribute("working-copy-class");
            System.out.println(String.format("Adding SCM: %s -> (%s, %s, %s)", name, configClassName, factoryClassName, wcClassName == null ? "<none>" : wcClassName));

            Class factoryClazz = loadClass(extension, factoryClassName);
            try
            {
                //noinspection unchecked
                clientFactory.register(configClazz, factoryClazz);
                if (wcClassName != null)
                {
                    Class wcClazz = loadClass(extension, wcClassName);
                    WorkingCopyFactory.registerType(name, wcClazz);
                }
            }
            catch (Exception e)
            {
                LOG.severe(e);
                handleExtensionError(extension, e);
            }
            catch (NoClassDefFoundError e)
            {
                LOG.severe(e);
                handleExtensionError(extension, e);
            }
        }
    }

    public void removeExtension(IExtension extension, Object[] objects)
    {
        // Uninstall not currently supported
        System.out.println("extension removed: " + extension.getContributor().getName());
    }

    public void setScmClientFactory(DelegateScmClientFactory clientFactory)
    {
        this.clientFactory = clientFactory;
    }
}
