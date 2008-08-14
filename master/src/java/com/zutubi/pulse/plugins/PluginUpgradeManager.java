package com.zutubi.pulse.plugins;

import com.zutubi.pulse.upgrade.UpgradeTask;
import com.zutubi.pulse.upgrade.UpgradeableComponent;
import com.zutubi.pulse.upgrade.UpgradeableComponentSource;
import com.zutubi.util.bean.ObjectFactory;
import com.zutubi.util.logging.Logger;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IContributor;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 *
 *
 */
public class PluginUpgradeManager implements UpgradeableComponentSource
{
    private static final Logger LOG = Logger.getLogger(PluginUpgradeManager.class);

    private static final String EXTENSION_POINT_ID = "com.zutubi.pulse.core.upgrade";

    private PluginManager pluginManager;
    private ObjectFactory objectFactory;
    
    private IExtensionRegistry extensionRegistry;

    private boolean upgradeRequired = false;
    private List<UpgradeableComponent> upgradeableComponents;

    public void init() throws PluginException
    {
        // if version changes were detected by the plugin system, we need to deal with them now.

        extensionRegistry = pluginManager.getExtensionRegistry();

        IExtensionPoint extensionPoint = extensionRegistry.getExtensionPoint(EXTENSION_POINT_ID);

        if (extensionPoint == null)
        {
            LOG.error("Failed to locate the plugin upgrade extension point.  Please ensure that the pulse core bundle is deployed.");
            return;
        }

        if (pluginManager.isVersionChangeDetected())
        {
            // Get a list of the plugins for which the version has changed.
            List<Plugin> upgradedPlugins = new LinkedList<Plugin>();

            PluginRegistry registry = pluginManager.getPluginRegistry();
            for (Plugin plugin : pluginManager.getPlugins())
            {
                if (plugin.getState() == Plugin.State.VERSION_CHANGE)
                {
                    upgradedPlugins.add(plugin);
                }
            }

            // Construct a map of all of the existing upgrade tasks registered via the upgrade extension point.
            // side note: The registration of these extensions occurs during the resolution of the plugins by
            //            the plugin manager.  There is a strong temporal association here.
            Map<String, List<UpgradeTaskHolder>> definedUpgradeTasks = new HashMap<String, List<UpgradeTaskHolder>>();

            IExtension[] extensions = extensionPoint.getExtensions();

            for (IExtension extension : extensions)
            {
                IContributor contributor = extension.getContributor();
                if (!definedUpgradeTasks.containsKey(contributor.getName()))
                {
                    definedUpgradeTasks.put(contributor.getName(), new LinkedList<UpgradeTaskHolder>());
                }
                
                List<UpgradeTaskHolder> tasks = definedUpgradeTasks.get(contributor.getName());

                IConfigurationElement[] configElements = extension.getConfigurationElements();
                for (IConfigurationElement config : configElements)
                {
                    tasks.add(new UpgradeTaskHolder(config));
                }
            }

            Map<String, List<UpgradeTaskHolder>> requiredUpgradeTasks = new HashMap<String, List<UpgradeTaskHolder>>();
            for (Plugin plugin : upgradedPlugins)
            {
                Version installedVersion = plugin.getVersion();
                Version registryVersion = new Version(registry.getEntry(plugin.getId()).get(PluginManager.PLUGIN_VERSION_KEY));

                List<UpgradeTaskHolder> tasks = definedUpgradeTasks.get(plugin.getId());
                if (tasks != null && tasks.size() > 0)
                {
                    requiredUpgradeTasks.put(plugin.getId(), new LinkedList<UpgradeTaskHolder>());
                    List<UpgradeTaskHolder> requiredPluginUpgradeTasks = requiredUpgradeTasks.get(plugin.getId());
                    for (UpgradeTaskHolder task : tasks)
                    {
                        // > registry
                        // <= installed
                        Version taskVersion = new Version(task.getVersion());
                        if (registryVersion.compareTo(taskVersion) < 0 && taskVersion.compareTo(installedVersion) <= 0)
                        {
                            // need to execute this upgrade task.
                            requiredPluginUpgradeTasks.add(task);
                        }
                    }
                    if (requiredPluginUpgradeTasks.size() == 0)
                    {
                        // no upgrade tasks are required.  So we can upgrade the plugins registry version now and
                        // clear the version changed state.
                        requiredUpgradeTasks.remove(plugin.getId());
                        try
                        {
                            registerVersionUpgrade(plugin);
                        }
                        catch (IOException e)
                        {
                            LOG.error(e);
                        }
                    }
                }
                else
                {
                    try
                    {
                        registerVersionUpgrade(plugin);
                    }
                    catch (IOException e)
                    {
                        LOG.error(e);
                    }
                }
            }

            upgradeRequired = requiredUpgradeTasks.size() > 0;
            if (upgradeRequired)
            {
                upgradeableComponents = new LinkedList<UpgradeableComponent>();
                // create upgradeable components
                for (String pluginId : requiredUpgradeTasks.keySet())
                {
                    Plugin plugin = pluginManager.getPlugin(pluginId);
                    List<UpgradeTaskHolder> taskHolders = requiredUpgradeTasks.get(pluginId);

                    // sort the upgrade tasks holders.

                    List<UpgradeTask> upgradeTasks = new LinkedList<UpgradeTask>();
                    for (UpgradeTaskHolder holder : taskHolders)
                    {
                        try
                        {
                            // instantiate the tasks.
                            Class<UpgradeTask> upgradeClazz = (Class<UpgradeTask>) plugin.loadClass(holder.getClazz());
                            
                            // create the upgradeableComponent.
                            upgradeTasks.add(objectFactory.buildBean(upgradeClazz));
                        }
                        catch (Exception e)
                        {
                            LOG.error(e);
                        }
                    }

                    upgradeableComponents.add(new PluginUpgradeableComponent(registry, plugin, upgradeTasks));
                }
            }
        }
    }

    private void registerVersionUpgrade(Plugin plugin) throws IOException, PluginException
    {
        PluginRegistry pluginRegistry = pluginManager.getPluginRegistry();
        
        // should be recording the new version of the plugin to which we just upgraded.
        PluginRegistryEntry entry = pluginRegistry.getEntry(plugin.getId());

        Version oldVersion = entry.getVersion();
        Version newVersion = plugin.getVersion();

        // log that we have upgraded from oldversion -> newversion.
        LOG.info("Plugin '"+plugin.getId()+"' has been upgraded from " + oldVersion + " to " + newVersion + ".");
        entry.put(PluginManager.PLUGIN_VERSION_KEY, newVersion.toString());
        pluginRegistry.flush();
        plugin.resolve();
    }

    public boolean isUpgradeRequired()
    {
        return upgradeRequired;
    }

    public List<UpgradeableComponent> getUpgradeableComponents()
    {
        return upgradeableComponents;
    }

    public void setPluginManager(PluginManager pluginManager)
    {
        this.pluginManager = pluginManager;
    }

    public void setObjectFactory(ObjectFactory objectFactory)
    {
        this.objectFactory = objectFactory;
    }

    private static class UpgradeTaskHolder
    {
        private IConfigurationElement configElement;

        public UpgradeTaskHolder(IConfigurationElement configElement)
        {
            this.configElement = configElement;
        }

        private String getVersion()
        {
            return configElement.getAttribute("version");
        }

        private String getClazz()
        {
            return configElement.getAttribute("class");
        }
    }

    private class PluginUpgradeableComponent implements UpgradeableComponent
    {
        private List<UpgradeTask> upgradeTasks;

        private Plugin plugin;

        public PluginUpgradeableComponent(PluginRegistry pluginRegistry, Plugin plugin, List<UpgradeTask> tasks)
        {
            this.plugin = plugin;
            this.upgradeTasks = tasks;
        }

        public boolean isUpgradeRequired()
        {
            return true;
        }

        public List<UpgradeTask> getUpgradeTasks()
        {
            return upgradeTasks;
        }

        public void upgradeStarted()
        {
            // callback
        }

        public void upgradeCompleted()
        {
            try
            {
                registerVersionUpgrade(plugin);
            }
            catch (Exception e)
            {
                LOG.error(e);
            }
        }

        public void upgradeAborted()
        {
            // callback
            // upgrade aborted?.. what happens to any version_change plugins... disable? - at least temporarily..
        }
    }
}
