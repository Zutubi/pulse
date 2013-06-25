package com.zutubi.pulse.master.plugins;

import com.zutubi.pulse.core.plugins.*;
import com.zutubi.pulse.master.upgrade.UpgradeTask;
import com.zutubi.pulse.master.upgrade.UpgradeableComponent;
import com.zutubi.pulse.master.upgrade.UpgradeableComponentSource;
import com.zutubi.util.bean.ObjectFactory;
import com.zutubi.util.logging.Logger;
import org.eclipse.core.runtime.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Manages the discovery and execution of upgrade tasks for plugins.
 */
public class PluginUpgradeManager implements UpgradeableComponentSource
{
    private static final Logger LOG = Logger.getLogger(PluginUpgradeManager.class);

    private static final String EXTENSION_POINT_ID = "com.zutubi.pulse.core.upgrade";
    public static final String PLUGIN_VERSION_KEY = "plugin.version";

    private PluginManager pluginManager;
    private ObjectFactory objectFactory;

    private boolean upgradeRequired = false;
    private List<UpgradeableComponent> upgradeableComponents;

    public void init() throws PluginException
    {
        IExtensionRegistry extensionRegistry = pluginManager.getExtensionRegistry();
        IExtensionPoint extensionPoint = extensionRegistry.getExtensionPoint(EXTENSION_POINT_ID);

        if (extensionPoint == null)
        {
            LOG.error("Failed to locate the plugin upgrade extension point.  Please ensure that the pulse core bundle is deployed.");
            return;
        }

        List<Plugin> upgradedPlugins = processPluginVersions();
        if (upgradedPlugins.size() > 0)
        {
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

            PluginRegistry registry = pluginManager.getPluginRegistry();
            Map<String, List<UpgradeTaskHolder>> requiredUpgradeTasks = new HashMap<String, List<UpgradeTaskHolder>>();
            for (Plugin plugin : upgradedPlugins)
            {
                PluginVersion installedVersion = plugin.getVersion();
                PluginVersion registryVersion = new PluginVersion(registry.getEntry(plugin.getId()).get(PLUGIN_VERSION_KEY));

                List<UpgradeTaskHolder> tasks = definedUpgradeTasks.get(plugin.getId());
                if (tasks != null && tasks.size() > 0)
                {
                    requiredUpgradeTasks.put(plugin.getId(), new LinkedList<UpgradeTaskHolder>());
                    List<UpgradeTaskHolder> requiredPluginUpgradeTasks = requiredUpgradeTasks.get(plugin.getId());
                    for (UpgradeTaskHolder task : tasks)
                    {
                        // > registry
                        // <= installed
                        PluginVersion taskVersion = new PluginVersion(task.getVersion());
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
                            @SuppressWarnings("unchecked")
                            Class<UpgradeTask> upgradeClazz = (Class<UpgradeTask>) plugin.loadClass(holder.getClazz());
                            
                            // create the upgradeableComponent.
                            upgradeTasks.add(objectFactory.buildBean(upgradeClazz));
                        }
                        catch (Exception e)
                        {
                            LOG.error(e);
                        }
                    }

                    upgradeableComponents.add(new PluginUpgradeableComponent(plugin, upgradeTasks));
                }
            }
        }
    }

    private List<Plugin> processPluginVersions() throws PluginException
    {
        PluginRegistry registry = pluginManager.getPluginRegistry();
        List<Plugin> upgradedPlugins = new LinkedList<Plugin>();
        boolean flushRequired = false;
        for (Plugin plugin: pluginManager.getPlugins())
        {
            PluginRegistryEntry entry = registry.getEntry(plugin.getId());
            if (entry.containsKey(PLUGIN_VERSION_KEY))
            {
                String version = entry.get(PLUGIN_VERSION_KEY);
                if (version == null)
                {
                    LOG.warning("Unexpected null version string in plugin registry for " + plugin.getId() + ".");
                    continue;
                }

                PluginVersion registryVersion = new PluginVersion(version);
                // we should check for older versions here... can we go back?, and if we do, what happens to the
                // registry version.
                if (registryVersion.compareTo(plugin.getVersion()) != 0)
                {
                    upgradedPlugins.add(plugin);
                }
            }
            else
            {
                entry.put(PLUGIN_VERSION_KEY, plugin.getVersion().toString());
                flushRequired = true;
            }
        }

        if (flushRequired)
        {
            try
            {
                registry.flush();
            }
            catch (IOException e)
            {
                throw new PluginException("Unable to flush plugin registry: " + e.getMessage(), e);
            }
        }

        return upgradedPlugins;
    }

    private void registerVersionUpgrade(Plugin plugin) throws IOException, PluginException
    {
        PluginRegistry pluginRegistry = pluginManager.getPluginRegistry();
        
        // should be recording the new version of the plugin to which we just upgraded.
        PluginRegistryEntry entry = pluginRegistry.getEntry(plugin.getId());

        PluginVersion oldVersion = new PluginVersion(entry.get(PLUGIN_VERSION_KEY));
        PluginVersion newVersion = plugin.getVersion();

        LOG.info("Plugin '" + plugin.getId() + "' has been upgraded from " + oldVersion + " to " + newVersion + ".");
        entry.put(PLUGIN_VERSION_KEY, newVersion.toString());
        pluginRegistry.flush();
    }

    public boolean isUpgradeRequired()
    {
        return upgradeRequired;
    }

    public boolean isUpgradeRequired(int fromBuildNumber, int toBuildNumber)
    {
        // FIXME: Plugin upgrades use different build numbers, so we don't know...
        // This is OK at the moment (no third-party plugins with upgrades), but we need a better system (the archive or
        // records themselves need to include plugin version information).
        return false;
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

        public PluginUpgradeableComponent(Plugin plugin, List<UpgradeTask> tasks)
        {
            this.plugin = plugin;
            this.upgradeTasks = tasks;
        }

        public boolean isUpgradeRequired()
        {
            return true;
        }

        public boolean isUpgradeRequired(int fromBuildNumber, int toBuildNumber)
        {
            /** FIXME see {@link PluginUpgradeManager#isUpgradeRequired(int, int)}. */
            return false;
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
