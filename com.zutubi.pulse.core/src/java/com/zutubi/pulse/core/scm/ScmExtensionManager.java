package com.zutubi.pulse.core.scm;

import com.zutubi.pulse.core.plugins.AbstractExtensionManager;
import com.zutubi.pulse.core.plugins.PluginManager;
import com.zutubi.pulse.core.scm.config.api.ScmConfiguration;
import com.zutubi.util.logging.Logger;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.dynamichelpers.IExtensionTracker;

import java.util.List;

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
/*  Extension point configuration:
        <extension point="com.zutubi.pulse.core.config">
            <config class="aaaa"/>
        </extension>

        <extension point="com.zutubi.pulse.core.scms">
            <scm name="xxx" factory-class="yyy" working-copy-class="zzzz"/>
        </extension>
*/
        try
        {
            String name = config.getAttribute("name");

            List<IConfigurationElement> configElements = getConfigElements(extension);

            if (configElements.size() != 1)
            {
                LOG.warning("Expected at one configuration extension for scm '%s' " +
                        "but instead found %i", name, configElements.size());
                //TODO: should the plugin be disabled?.
                return;
            }

            IConfigurationElement configElement = configElements.get(0);

            String configClassName = configElement.getAttribute("class");
            Class configClazz = loadClass(extension, configClassName);

            if (!ScmConfiguration.class.isAssignableFrom(configClazz))
            {
                LOG.warning("Configuration class '" + configClazz.getName() + "' must be " +
                        "an extension of " + ScmConfiguration.class.getName());
                //TODO: should the plugin be disabled?.
                return;
            }

            ScmConfiguration configInstance = (ScmConfiguration) configClazz.newInstance();

            if (!configInstance.getType().equals(name))
            {
                LOG.warning("Configuration class '" + configClazz + "'.getType() is expected " +
                        "to return '" + name +"' but instead returns '" + configInstance.getType() +"'");
                //TODO: should the plugin be disabled?.
                return;
            }

            String factoryClassName = config.getAttribute("factory-class");
            String wcClassName = config.getAttribute("working-copy-class");
            
            if (PluginManager.VERBOSE_EXTENSIONS)
            {
                System.out.println(String.format("Adding SCM: %s -> (%s, %s, %s)", name, configClassName, factoryClassName, wcClassName == null ? "<none>" : wcClassName));
            }

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
            catch (Throwable e)
            {
                LOG.warning(e);
                handleExtensionError(extension, e);
            }
        }
        catch (Exception e)
        {
            LOG.warning(e);
            handleExtensionError(extension, e);
        }
    }

    public void removeExtension(IExtension extension, Object[] objects)
    {
        // Uninstall not currently supported
        if (PluginManager.VERBOSE_EXTENSIONS)
        {
            System.out.println("extension removed: " + extension.getContributor().getName());
        }
    }

    public void setScmClientFactory(DelegateScmClientFactory clientFactory)
    {
        this.clientFactory = clientFactory;
    }
}
