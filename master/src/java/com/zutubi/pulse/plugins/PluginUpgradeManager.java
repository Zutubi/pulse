package com.zutubi.pulse.plugins;

import com.zutubi.pulse.upgrade.UpgradeableComponent;
import com.zutubi.pulse.upgrade.UpgradeableComponentSource;
import com.zutubi.pulse.upgrade.UpgradeTask;
import com.zutubi.util.bean.ObjectFactory;
import com.zutubi.util.logging.Logger;

import java.util.List;
import java.util.Map;
import java.util.LinkedList;
import java.util.HashMap;
import java.io.IOException;

import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IContributor;

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
            // now i need the plugins with version changes, the current registry version, and the version of the plugin.
            List<Plugin> upgradedPlugins = new LinkedList<Plugin>();

            PluginRegistry registry = pluginManager.getPluginRegistry();
            for (Plugin plugin : pluginManager.getPlugins())
            {
                Map<String, String> entry = registry.getEntry(plugin.getId());
                if (entry.containsKey(PluginManager.PLUGIN_VERSION_KEY))
                {
                    Version registryVersion = new Version(entry.get(PluginManager.PLUGIN_VERSION_KEY));
                    if (registryVersion.compareTo(plugin.getVersion()) < 0) 
                    {
                        upgradedPlugins.add(plugin);
                    }
                }
            }

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
                    UpgradeTaskHolder holder = new UpgradeTaskHolder();
                    holder.version = config.getAttribute("version");
                    holder.clazz = config.getAttribute("class");
                    tasks.add(holder);
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
                        Version taskVersion = new Version(task.version);
                        if (registryVersion.compareTo(taskVersion) < 0 && taskVersion.compareTo(installedVersion) <= 0)
                        {
                            // need to execute this upgrade task.
                            requiredPluginUpgradeTasks.add(task);
                        }
                    }
                    if (requiredPluginUpgradeTasks.size() == 0)
                    {
                        requiredUpgradeTasks.remove(plugin.getId());
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
                            Class<UpgradeTask> upgradeClazz = (Class<UpgradeTask>) plugin.loadClass(holder.clazz);
                            
                            // create the upgradeableComponent.
                            upgradeTasks.add(objectFactory.buildBean(upgradeClazz));
                        }
                        catch (Exception e)
                        {
                            e.printStackTrace();
                        }
                    }

                    upgradeableComponents.add(new PluginUpgradeableComponent(registry, plugin, upgradeTasks));
                }
            }
        }
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
        String version;
        String clazz;
    }

    private static class PluginUpgradeableComponent implements UpgradeableComponent
    {
        private List<UpgradeTask> upgradeTasks;

        private Plugin plugin;

        private PluginRegistry pluginRegistry;

        public PluginUpgradeableComponent(PluginRegistry pluginRegistry, Plugin plugin, List<UpgradeTask> tasks)
        {
            this.pluginRegistry = pluginRegistry;
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
// callback
                // should be recording the new version of the plugin to which we just upgraded.
                Map<String, String> entry = pluginRegistry.getEntry(plugin.getId());

                Version oldVersion = new Version(entry.get(PluginManager.PLUGIN_VERSION_KEY));
                Version newVersion = plugin.getVersion();

                // log that we have upgraded from oldversion -> newversion.

                entry.put(PluginManager.PLUGIN_VERSION_KEY, newVersion.toString());
                pluginRegistry.flush();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }

        public void upgradeAborted()
        {
            // callback
        }
    }
}
