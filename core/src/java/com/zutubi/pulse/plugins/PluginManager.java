package com.zutubi.pulse.plugins;

import com.zutubi.pulse.bootstrap.ComponentContext;
import com.zutubi.pulse.plugins.osgi.Equinox;
import com.zutubi.pulse.plugins.osgi.OSGiFramework;
import com.zutubi.pulse.util.FileSystemUtils;
import com.zutubi.util.CollectionUtils;
import com.zutubi.util.IOUtils;
import com.zutubi.util.Mapping;
import com.zutubi.util.Predicate;
import com.zutubi.util.TextUtils;
import com.zutubi.util.logging.Logger;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.RegistryFactory;
import org.eclipse.core.runtime.dynamichelpers.ExtensionTracker;
import org.eclipse.core.runtime.dynamichelpers.IExtensionTracker;
import org.eclipse.osgi.service.resolver.BundleDescription;
import org.eclipse.osgi.service.resolver.BundleSpecification;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 *
 *
 */
public class PluginManager
{
    public static final String CONFIG_EXTENSION_POINT = "com.zutubi.pulse.core.config";

    private static final Logger LOG = Logger.getLogger(PluginManager.class);

    protected Equinox equinox;

    private PluginRegistry registry;

    private PluginPaths paths;

    private List<LocalPlugin> plugins = new LinkedList<LocalPlugin>();

    // special plugins that are loaded ahead of all others.  Upgrade handling does not apply to these plugins.
    private List<LocalPlugin> internalPlugins = new LinkedList<LocalPlugin>();

    private IExtensionRegistry extensionRegistry;
    private IExtensionTracker extensionTracker;

    //-- plugin registry keys. --

    private static final String PLUGIN_STATE_KEY = "plugin.state";
    private static final String PLUGIN_TYPE_KEY = "plugin.type";
    public static final String PLUGIN_VERSION_KEY = "plugin.version";
    public static final String PLUGIN_SOURCE_KEY = "plugin.uri";
    private static final String PLUGIN_PENDING_KEY = "plugin.pending.action";
    private static final String UPGRADE_SOURCE_KEY = "plugin.upgrade.source";

    //--- pending action strings

    private static final String UNINSTALL_PENDING_ACTION = "uninstall";
    private static final String DISABLE_PENDING_ACTION = "disable";
    private static final String UPGRADE_PENDING_ACTION = "upgrade";

    private static final PluginFileFilter PLUGIN_FILTER = new PluginFileFilter();

    private List<ExtensionManager> extensionManagers = new LinkedList<ExtensionManager>();

    private boolean versionChangeDetected = false;

    private enum State
    {
        ENABLED, DISABLED, UNINSTALLED
    }

    public PluginManager()
    {
    }

    public void init() throws Exception
    {
        // Step 1: process any pending actions.
        //  - these need to be executed BEFORE we start working with the underlying equinox system, in particular,
        //    before we start wiring things together.
        registry = new PluginRegistry(paths.getPluginRegistryDir());

        processPendingActions();

        // Step 2: start the embedded plugin container
        //  - configure start equinox
        //  - install and startup the internal plugins.
        equinox = new Equinox();

        // setup the configuration.
        equinox.setProperty(OSGiFramework.OSGI_CONFIGURATION_AREA, paths.getOsgiConfigurationDir().getAbsolutePath());
        equinox.start();

        startupInternalPlugins();

        // extension registry is not available until the internal plugins containing the eclipse registry have been loaded.
        extensionRegistry = RegistryFactory.getRegistry();
        extensionTracker = new ExtensionTracker(extensionRegistry);

        // Step 3: scan the various directories and update the registry accordingly.
        //  - install the pre-packaged plugins, unless they have been explicitly marked as uninstalled.
        //  - register new plugins, mark uninstalls as uninstalled.

        // run scans over the directories before checking for manual uninstalls so that manual upgrades are not
        // miss interpreted.
        scanPrepackagedPlugins();
        scanUserPlugins();
        scanForManualUninstalls();

        // Step 4: plugin startup
        //  - install and resolve the plugins. (deal with unresolved plugins - provide as much feedback as possible).
        //  - check for version changes.  Those plugins with version changes may require upgrades - leave that to
        //    the plugin upgrade process
        //  - start the plugins that we can start.

        List<LocalPlugin> installedPlugins = new LinkedList<LocalPlugin>();

        //TODO: create handles for all plugins - enabled and disabled.
        for (String id : registry.getRegistrations())
        {
            Map<String, String> entry = registry.getEntry(id);

            // initialise the installed plugins.
            if (entry.containsKey(PLUGIN_SOURCE_KEY))
            {
                try
                {
                    LocalPlugin plugin = createPluginHandle(entry.get(PLUGIN_SOURCE_KEY), Plugin.Type.valueOf(entry.get(PLUGIN_TYPE_KEY)));
                    plugin.setState(Plugin.State.INSTALLED);
                    State targetState = State.valueOf(entry.get(PLUGIN_STATE_KEY));
                    switch (targetState)
                    {
                        case ENABLED:
                            installedPlugins.add(plugin);
                            break;
                        case DISABLED:
                            plugin.setState(Plugin.State.DISABLED);
                            break;
                        default:
                            break;
                    }
                    plugins.add(plugin);
                }
                catch (IllegalArgumentException e)
                {
                    LOG.warning("Unexpected IAE while setting up plugin handle for " + id + ".", e);
                }
            }
        }

        // enable all of the installed plugins - enable in batch.
        // - this processing is exactly the same as that done during the enable, except on a bulk
        versionChangeDetected = false;
        for (LocalPlugin plugin : installedPlugins)
        {
            Map<String, String> entry = registry.getEntry(plugin.getId());
            if (entry.containsKey(PLUGIN_VERSION_KEY))
            {
                String registryVersionString = entry.get(PLUGIN_VERSION_KEY);
                if (!TextUtils.stringSet(registryVersionString))
                {
                    LOG.warning("Unexpected null version string in plugin registry for " + plugin.getId() + ".");
                    continue;
                }
                Version registryVersion = new Version(registryVersionString);
                if (registryVersion.compareTo(plugin.getVersion()) != 0)
                {
                    plugin.setState(Plugin.State.VERSION_CHANGE);
                    versionChangeDetected = true;
                }
            }

            plugin.bundle = equinox.install(plugin.getSource());
            plugin.bundleDescription = equinox.getBundleDescription(plugin.getId(), plugin.getVersion().toString());
        }

        //NOTE: the extension works with resolved plugins, so we need to get a plugin to resolved before it can be upgrades. 

        // resolve them all.
        equinox.resolveBundles();

        //FIXME: Since we are resolving all of the bundles at once, we need to post process
        //FIXME: them and disable those that could not be resolved.
        //FIXME: This will also provide us with the opporunity to provide properly detailed error messages.

        List<LocalPlugin> resolvedPlugins = new LinkedList<LocalPlugin>();
        for (LocalPlugin plugin : installedPlugins)
        {
            switch (plugin.bundle.getState())
            {
                case Bundle.INSTALLED:
                    // resolve failed.
                    try
                    {
                        plugin.bundle.uninstall();
                        plugin.bundle = null;
                        plugin.bundleDescription = null;
                        plugin.setState(Plugin.State.DISABLED);
                        plugin.setErrorMessage("Failed to resolve bundle.");
                    }
                    catch (BundleException e)
                    {
                        LOG.warning("Failed to uninstall bundle. Disabling plugin '" + plugin.getId() + "'. Cause: " + e.getMessage(), e);
                    }
                    break;
                case Bundle.RESOLVED:
                case Bundle.STARTING:
                    resolvedPlugins.add(plugin);
                    break;
                default:
                    LOG.warning("Unexpected plugin state; plugin: " + plugin.getName() + "(" + plugin.getId() + "), state: " + plugin.bundle.getState());
            }
        }

        List<LocalPlugin> sortedPlugins = sortPlugins(resolvedPlugins);

        // c) if none have version change, then start them all.
        if (!versionChangeDetected)
        {
            for (LocalPlugin plugin : sortedPlugins)
            {
                // only want plugins that were successfully resolved.
                if (plugin.getState() == Plugin.State.INSTALLED)
                {
                    try
                    {
                        plugin.bundle.start(Bundle.START_TRANSIENT);
                        Map<String, String> entry = registry.getEntry(plugin.getId());
                        entry.put(PLUGIN_VERSION_KEY, plugin.getVersion().toString());
                        saveRegistry();

                        plugin.setState(Plugin.State.ENABLED);
                    }
                    catch (BundleException e)
                    {
                        plugin.bundle.uninstall();
                        plugin.bundle = null;
                        plugin.bundleDescription = null;
                        plugin.setState(Plugin.State.DISABLED);
                        plugin.setErrorMessage(e.getMessage());
                    }
                }
            }

        }
    }

    private void startupInternalPlugins() throws PluginException, BundleException
    {
        for(File file : paths.getInternalPluginStorageDir().listFiles())
        {
            LocalPlugin internalPlugin = createPluginHandle(file.toURI(), Plugin.Type.INTERNAL);
            internalPlugin.bundle = equinox.install(internalPlugin.getSource());
            internalPlugin.bundleDescription = equinox.getBundleDescription(internalPlugin.getId(), internalPlugin.getVersion().toString());
            internalPlugins.add(internalPlugin);
        }
        equinox.resolveBundles();
        internalPlugins = sortPlugins(internalPlugins);
        for (LocalPlugin plugin : internalPlugins)
        {
            plugin.bundle.start(Bundle.START_TRANSIENT);
            plugin.setState(Plugin.State.ENABLED);
        }
    }

    public boolean isVersionChangeDetected()
    {
        return versionChangeDetected;
    }

    protected void completeStartup()
    {

    }

    private void scanForManualUninstalls() throws PluginException
    {
        for (String id : registry.getRegistrations())
        {
            try
            {
                Map<String, String> entry = registry.getEntry(id);

                // check for manually uninstalled plugins.
                if (entry.containsKey(PLUGIN_SOURCE_KEY))
                {
                    String sourceUriString = entry.get(PLUGIN_SOURCE_KEY);
                    if (!TextUtils.stringSet(sourceUriString))
                    {
                        LOG.warning("Registry entry for plugin '" + id + "' is corrupt. It contains a null source string.");
                        continue;
                    }
                    File source = new File(new URI(sourceUriString));
                    if (!source.exists())
                    {
                        // looks like this plugin is no longer available.
                        entry.put(PLUGIN_STATE_KEY, State.UNINSTALLED.toString());
                        entry.remove(PLUGIN_SOURCE_KEY);
                        saveRegistry();
                    }
                }
            }
            catch (URISyntaxException e)
            {
                LOG.warning("Registry entry for plugin '" + id + "' is corrupt. Error: " + e.getMessage());
            }
        }
    }

    private void processPendingActions() throws PluginException
    {
        try
        {
            for (String id : registry.getRegistrations())
            {
                Map<String, String> entry = registry.getEntry(id);
                try
                {
                    // process the pending actions.
                    if (entry.containsKey(PLUGIN_PENDING_KEY))
                    {
                        String pendingAction = entry.get(PLUGIN_PENDING_KEY);
                        if (!TextUtils.stringSet(pendingAction))
                        {
                            LOG.warning("Registry entry for plugin '" + id + "' is corrupt. Missing pending action.");
                            continue;
                        }
                        if (pendingAction.equals(DISABLE_PENDING_ACTION))
                        {
                            entry.put(PLUGIN_STATE_KEY, State.DISABLED.toString());
                        }
                        else if (pendingAction.equals(UNINSTALL_PENDING_ACTION))
                        {
                            if (entry.containsKey(PLUGIN_SOURCE_KEY))
                            {
                                String source = entry.get(PLUGIN_SOURCE_KEY);
                                if (!TextUtils.stringSet(source))
                                {
                                    LOG.warning("Registry entry for plugin '" + id + "' is corrupt. Uninstall source not specified.");
                                }
                                File plugin = new File(new URI(source));
                                FileSystemUtils.delete(plugin);
                                entry.remove(PLUGIN_SOURCE_KEY);
                            }
                            entry.put(PLUGIN_STATE_KEY, State.UNINSTALLED.toString());
                        }
                        else if (pendingAction.equals(UPGRADE_PENDING_ACTION))
                        {
                            URI newSource = new URI(entry.get(UPGRADE_SOURCE_KEY));
                            upgradePluginSource(id, newSource);

                            // cleanup the temporary file.
                            File tmpPluginFile = new File(newSource);
                            FileSystemUtils.delete(tmpPluginFile);
                        }
                        entry.remove(PLUGIN_PENDING_KEY);
                        saveRegistry(); // this may be overly aggressive flushing.
                    }
                }
                catch (URISyntaxException e)
                {
                    LOG.warning("Registry entry for plugin '" + id + "' is corrupt. Error: " + e.getMessage());
                }
            }
        }
        catch (IOException e)
        {
            throw new PluginException(e);
        }
    }

    private void upgradePluginSource(String id, URI newSource) throws URISyntaxException, IOException, PluginException
    {
        // replace the old plugin, install and register the new source
        Map<String, String> entry = registry.getEntry(id);

        // delete the old
        File pluginFile = new File(new URI(entry.get(PLUGIN_SOURCE_KEY)));
        if (pluginFile.isDirectory())
        {
            FileSystemUtils.rmdir(pluginFile);
        }
        else if (pluginFile.isFile())
        {
            FileSystemUtils.delete(pluginFile);
        }

        // combination of uninstall the current plugin and installing the new.
        URI installedSource = downloadPlugin(newSource, paths.getPluginStorageDir());

        // register the plugin with the registry
        entry.put(PLUGIN_SOURCE_KEY, installedSource.toString());
        saveRegistry();
    }

    private void scanUserPlugins() throws PluginException
    {
        scanInplacePluginDirectory(paths.getPluginStorageDir(), Plugin.Type.USER);
    }

    private void scanPrepackagedPlugins() throws PluginException
    {
        if (paths.getPrepackagedPluginStorageDir() == null)
        {
            // During dev, the prepackaged plugins are compiled directly into the storage directory.
            // Lets just work with this for now.
            return;
        }

        // discover pre-packaged plugins.
        for (File file : paths.getPrepackagedPluginStorageDir().listFiles(PLUGIN_FILTER))
        {
            LocalPlugin plugin = createPluginHandle(file.toURI(), Plugin.Type.USER);
            try
            {
                if (!registry.isRegistered(plugin))
                {
                    // download and register - may want to make the default 'discovery' behaviour configurable?
                    URI installedSource = downloadPlugin(file.toURI(), paths.getPluginStorageDir());

                    registerPlugin(createPluginHandle(installedSource, Plugin.Type.USER));
                }
                else
                {
                    Map<String, String> entry = registry.getEntry(plugin.getId());

                    State state = State.valueOf(entry.get(PLUGIN_STATE_KEY));
                    if (state == State.UNINSTALLED)
                    {
                        // this plugin has been uninstalled, nothing further is required from it.
                        continue;
                    }

                    // version check. if new version available, mark it for pending upgrade.
                    String pluginSourceString = entry.get(PLUGIN_SOURCE_KEY);
                    if (!TextUtils.stringSet(pluginSourceString))
                    {
                        LOG.warning("Can not complete pre-packaged plugin version for " + plugin.getId() + ".  Installed source not available.");
                        continue;
                    }
                    LocalPlugin registeredPlugin = createPluginHandle(new URI(pluginSourceString), Plugin.Type.valueOf(entry.get(PLUGIN_TYPE_KEY)));

                    //TODO: provide the user with the opportunity to not upgrade to the new version. They may have an old version for a specific reason.

                    if (plugin.getVersion().compareTo(registeredPlugin.getVersion()) > 0)
                    {
                        try
                        {
                            upgradePluginSource(plugin.getId(), file.toURI());
                        }
                        catch (IOException e)
                        {
                            throw new PluginException(e);
                        }
                    }
                }
            }
            catch (URISyntaxException e)
            {
                LOG.warning("Registry entry for plugin '" + plugin.getId() + "' is corrupt. Error: " + e.getMessage());
            }
        }
    }

    private void scanInplacePluginDirectory(File dir, Plugin.Type type) throws PluginException
    {
        for (File file : dir.listFiles(PLUGIN_FILTER))
        {
            LocalPlugin plugin = createPluginHandle(file.toURI(), type);
            try
            {

                if (!registry.isRegistered(plugin))
                {
                    // plugin is already where we want it, just a matter of installing it.
                    registerPlugin(createPluginHandle(file.toURI(), type));
                }
                else
                {
                    Map<String, String> registryEntry = registry.getEntry(plugin.getId());

                    // is the current file the same as the registered file?
                    URI registryURI = new URI(registryEntry.get(PLUGIN_SOURCE_KEY));
                    if (registryURI.compareTo(file.toURI()) == 0)
                    {
                        continue;
                    }

                    // we always want to be using the latest version of the internal plugins, so update
                    // the registry with whatever we find. The system startup will detect the version change
                    // if one exists.
                    // NOTE: We are effectively re-registering each of the internal plugins each time round
                    registryEntry.put(PLUGIN_SOURCE_KEY, file.toURI().toString());
                    saveRegistry();

                    // Alternative - only update if a) no version specified, b) there is a known version increase.
                }
            }
            catch (URISyntaxException e)
            {
                LOG.warning("Registry entry for plugin '" + plugin.getId() + "' is corrupt. Error: " + e.getMessage());
            }
        }
    }

    public Plugin install(URI uri) throws PluginException
    {
        return install(uri, true);
    }

    public Plugin install(URI uri, boolean autostart) throws PluginException
    {
        return install(uri, null, autostart);
    }

    public Plugin install(URI uri, String filename, boolean autostart) throws PluginException
    {
        //TODO: check that the uri defines a valid plugin.
        //TODO: check that the plugin we download does not already exist.  We do not want to install the same plugin twice.

        // copy it into the internal plugin storage directory

        URI installedSource = downloadPlugin(uri, filename, paths.getPluginStorageDir());

        LocalPlugin installedPlugin = createPluginHandle(installedSource, Plugin.Type.USER);

        // register the plugin with the registry
        registerPlugin(installedPlugin);
        installedPlugin.setState(Plugin.State.INSTALLED);
        plugins.add(installedPlugin);

        // FIXME: if registration fails, then we will want to delete the installed source. Chances are that the
        // user will try again, so make sure we leave nothing behing at this stage.

        // and optionally enable it.
        if (autostart)
        {
            enablePlugin(installedPlugin);
        }
        else
        {
            disablePlugin(installedPlugin);
        }

        return installedPlugin;
    }

    void disablePlugin(LocalPlugin plugin) throws PluginException
    {
        // assumption: we can disable

        // process the disable.  this is the same thing that is done if PENDING_DISABLE is encountered.
        Map<String, String> entry = registry.getEntry(plugin.getId());
        entry.put(PLUGIN_STATE_KEY, State.DISABLED.toString());
        saveRegistry();
        plugin.setState(Plugin.State.DISABLED);
    }

    void requestDisable(LocalPlugin plugin) throws PluginException
    {
        // the plugin is currently enabled, so we need to request a pending action.
        Map<String, String> entry = registry.getEntry(plugin.getId());
        entry.put(PLUGIN_PENDING_KEY, DISABLE_PENDING_ACTION);
        saveRegistry();
        plugin.setState(Plugin.State.DISABLING);
    }

    void uninstallPlugin(LocalPlugin plugin) throws PluginException
    {
        try
        {
            Map<String, String> entry = registry.getEntry(plugin.getId());
            if (entry.containsKey(PLUGIN_SOURCE_KEY))
            {
                String source = entry.get(PLUGIN_SOURCE_KEY);
                if (!TextUtils.stringSet(source))
                {
                    LOG.warning("Unable to remove source for plugin " + plugin.getId() + " during uninstall.  Source not known.");
                }
                else
                {
                    File pluginFile = new File(new URI(source));
                    if (pluginFile.isDirectory())
                    {
                        FileSystemUtils.rmdir(pluginFile);
                    }
                    else if (pluginFile.isFile())
                    {
                        FileSystemUtils.delete(pluginFile);
                    }
                }

                entry.remove(PLUGIN_SOURCE_KEY);
            }
            entry.put(PLUGIN_STATE_KEY, State.UNINSTALLED.toString());
            saveRegistry();

            plugin.setState(Plugin.State.UNINSTALLED);
        }
        catch (URISyntaxException e)
        {
            throw new PluginException(e);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    void requestUninstall(LocalPlugin plugin) throws PluginException
    {
        // the plugin is currently enabled, so we need to request a pending action.
        Map<String, String> entry = registry.getEntry(plugin.getId());
        entry.put(PLUGIN_PENDING_KEY, UNINSTALL_PENDING_ACTION);
        saveRegistry();

        plugin.setState(Plugin.State.UNINSTALLING);
    }

    void enablePlugin(LocalPlugin plugin) throws PluginException
    {
        // assumption: we can enable

        Map<String, String> entry = registry.getEntry(plugin.getId());
        entry.put(PLUGIN_STATE_KEY, State.ENABLED.toString());
        saveRegistry();

        // check version, if it has changed, then set the plugin state to VERSION_CHANGED.
        try
        {
            if (entry.containsKey(PLUGIN_VERSION_KEY))
            {
                Version registryVersion = new Version(entry.get(PLUGIN_VERSION_KEY));
                if (registryVersion.compareTo(plugin.getVersion()) != 0)
                {
                    plugin.setState(Plugin.State.VERSION_CHANGE);
                }
            }
        }
        catch (IllegalArgumentException e)
        {
            throw new PluginException("Version check during plugin enable failed. Cause: IllegalArgumentException - " + e.getMessage(), e);
        }

        //TODO: OK, so this plugin is enabled, when does the extension manager receive a callback
        //      notifying of this change in state?.

        // activate this plugin within osgi.
        try
        {
            if (plugin.getState() == Plugin.State.VERSION_CHANGE)
            {
                // install only.
                plugin.bundle = equinox.resolve(plugin.getSource());
                plugin.bundleDescription = equinox.getBundleDescription(plugin.getId(), plugin.getVersion().toString());
            }
            else
            {
                // fully activate the plugin.
                plugin.bundle = equinox.activate(plugin.getSource());
                plugin.bundleDescription = equinox.getBundleDescription(plugin.getId(), plugin.getVersion().toString());

                entry.put(PLUGIN_VERSION_KEY, plugin.getVersion().toString());
                saveRegistry();
                plugin.setState(Plugin.State.ENABLED);
            }
        }
        catch (Exception e)
        {
            try
            {
                if (plugin.bundle != null)
                {
                    plugin.bundle.uninstall();
                }
            }
            catch (BundleException e1)
            {
                LOG.warning(e1);
            }

            plugin.bundle = null;
            plugin.bundleDescription = null;
            plugin.setState(Plugin.State.DISABLED);
            plugin.setErrorMessage(e.getMessage());
        }
    }

    Plugin upgradePlugin(LocalPlugin currentPlugin, URI newSource) throws PluginException
    {
        try
        {
            // replace the old plugin, install and register the new source
            File pluginFile = new File(currentPlugin.getSource());
            if (pluginFile.isDirectory())
            {
                FileSystemUtils.rmdir(pluginFile);
            }
            else if (pluginFile.isFile())
            {
                FileSystemUtils.delete(pluginFile);
            }

            plugins.remove(currentPlugin);

            // combination of uninstall the current plugin and installing the new.

            URI installedSource = downloadPlugin(newSource, paths.getPluginStorageDir());

            LocalPlugin installedPlugin = createPluginHandle(installedSource, Plugin.Type.USER);

            // register the plugin with the registry
            Map<String, String> registryEntry = registry.register(installedPlugin);
            registryEntry.put(PLUGIN_SOURCE_KEY, installedSource.toString());
            saveRegistry();

            plugins.add(installedPlugin);
            installedPlugin.setState(Plugin.State.DISABLED);

            return installedPlugin;
        }
        catch (IOException e)
        {
            throw new PluginException(e);
        }
    }

    void cancelUpgrade(LocalPlugin plugin) throws PluginException
    {
        try
        {
            Map<String, String> entry = registry.getEntry(plugin.getId());

            File pluginFile = new File(new URI(entry.get(UPGRADE_SOURCE_KEY)));
            if (pluginFile.isDirectory())
            {
                FileSystemUtils.rmdir(pluginFile);
            }
            else if (pluginFile.isFile())
            {
                FileSystemUtils.delete(pluginFile);
            }

            entry.remove(PLUGIN_PENDING_KEY);
            entry.remove(UPGRADE_SOURCE_KEY);
            saveRegistry();
            plugin.setState(Plugin.State.ENABLED);
        }
        catch (IOException e)
        {
            throw new PluginException(e);
        }
        catch (URISyntaxException e)
        {
            throw new PluginException(e); // internal error...
        }
    }

    void requestUpgrade(LocalPlugin plugin, URI newSource) throws PluginException
    {
        URI installedSource = downloadPlugin(newSource, paths.getPluginWorkDir());

        // the plugin is currently enabled, so we need to request a pending action.
        Map<String, String> registryEntry = registry.getEntry(plugin.getId());
        registryEntry.put(PLUGIN_PENDING_KEY, UPGRADE_PENDING_ACTION);
        registryEntry.put(UPGRADE_SOURCE_KEY, installedSource.toString());
        saveRegistry();
        plugin.setState(Plugin.State.UPDATING);
    }

    void resolveVersionChange(LocalPlugin plugin)
    {
        plugin.setState(Plugin.State.INSTALLED);

        try
        {
            // fully activate the plugin.
            plugin.bundle.start(Bundle.START_TRANSIENT);
            Map<String, String> registryEntry = registry.getEntry(plugin.getId());
            registryEntry.put(PLUGIN_VERSION_KEY, plugin.getVersion().toString());
            plugin.setState(Plugin.State.ENABLED);
        }
        catch (Exception e)
        {
            plugin.setState(Plugin.State.DISABLED);
            plugin.setErrorMessage(e.getMessage());
        }
    }

    private LocalPlugin createPluginHandle(String source, Plugin.Type type) throws PluginException
    {
        if (!TextUtils.stringSet(source))
        {
            throw new IllegalArgumentException("Can not create a plugin handle without a source string defined.");                
        }
        try
        {
            return createPluginHandle(new URI(source), type);
        }
        catch (URISyntaxException e)
        {
            throw new IllegalArgumentException(e);
        }
    }

    private LocalPlugin createPluginHandle(URI source, Plugin.Type type) throws PluginException
    {
        //TODO: use the plugin file filter here to validate.

        File file = new File(source);
        LocalPlugin plugin;
        if (file.isDirectory())
        {
            plugin = new DirectoryPlugin(file);
        }
        else if (file.isFile() && file.getName().endsWith(".jar"))
        {
            plugin = new JarFilePlugin(file);
        }
        else
        {
            throw new PluginException("Unsupported plugin file format: " + file.getAbsolutePath() + ". Expected a .jar file or a directory.");
        }

        plugin.manager = this;
        plugin.setType(type);

        return plugin;
    }

    private void saveRegistry() throws PluginException
    {
        try
        {
            registry.flush();
        }
        catch (IOException e)
        {
            throw new PluginException(e);
        }
    }

    private void checkInstallAndResolve(LocalPlugin plugin) throws BundleException
    {
        equinox.checkInstallAndResolve(plugin.manifest, plugin.source);
    }

    private Map<String, String> registerPlugin(Plugin plugin) throws PluginException
    {
        try
        {
            Map<String, String> registryEntry = registry.register(plugin);
            registryEntry.put(PLUGIN_SOURCE_KEY, plugin.getSource().toString());
            registryEntry.put(PLUGIN_STATE_KEY, State.ENABLED.toString());
            registryEntry.put(PLUGIN_TYPE_KEY, plugin.getType().toString());
            saveRegistry();
            return registryEntry;
        }
        catch (PluginException e)
        {
            registry.unregister(plugin.getId());
            throw e;
        }
    }

    public void destroy() throws Exception
    {
        equinox.stop();
    }

    public void setPluginPaths(PluginPaths paths)
    {
        this.paths = paths;
    }

    public List<Plugin> getPlugins()
    {
        return new LinkedList<Plugin>(plugins);
    }

    public List<Plugin> getDependentPlugins(LocalPlugin plugin)
    {
        return new LinkedList<Plugin>(getDependentPlugins(plugin, plugins, false));
    }

    public List<PluginRequirement> getRequiredPlugins(LocalPlugin plugin)
    {
        BundleDescription description = plugin.bundleDescription;
        if (description == null)
        {
            // It is the bundles description that tells us about the bundles requirements.
            // No description == no requirement information.  The plugin does have requirements,
            // but to get at them, we would need to read the raw bundle description outselves.
            return new LinkedList<PluginRequirement>();
        }

        BundleSpecification[] requiredBundles = description.getRequiredBundles();
        return CollectionUtils.map(requiredBundles, new Mapping<BundleSpecification, PluginRequirement>()
        {
            public PluginRequirement map(BundleSpecification bundleSpecification)
            {
                return new PluginRequirement(bundleSpecification.getName(),
                        convertVersionRange(bundleSpecification.getVersionRange()),
                        getPlugin(bundleSpecification.getName()));
            }
        });
    }

    private VersionRange convertVersionRange(org.eclipse.osgi.service.resolver.VersionRange versionRange)
    {
        return new VersionRange(convertVersion(versionRange.getMinimum()), versionRange.getIncludeMinimum(), convertVersion(versionRange.getMaximum()), versionRange.getIncludeMaximum());
    }

    private Version convertVersion(org.osgi.framework.Version version)
    {
        return new Version(version.getMajor(), version.getMinor(), version.getMicro(), version.getQualifier());
    }

    private URI downloadPlugin(URI source, File dest) throws PluginException
    {
        return downloadPlugin(source, null, dest);
    }

    /**
     * Copy the source contents into the destination directory.
     *
     * @param source
     * @param dest
     * @return the URI for the copied content.
     * @throws PluginException if a problem occurs with the copy.
     */
    private URI downloadPlugin(URI source, String filename, File dest) throws PluginException
    {
        if (filename == null)
        {
            filename = deriveName(source);
        }

        File downloadedFile = new File(dest, filename);

        if (downloadedFile.exists())
        {
            throw new PluginException("Can not download plugin.  Plugin with name " + downloadedFile.getName() + " already exists.");
        }

        File tmpFile = null;
        InputStream is = null;
        OutputStream os = null;

        try
        {
            is = source.toURL().openStream();

            tmpFile = new File(downloadedFile.getAbsolutePath() + ".tmp");

            if (!tmpFile.getParentFile().exists() && !tmpFile.getParentFile().mkdirs())
            {
                throw new IOException("Failed to download plugin. Unable to create new directory: " + tmpFile.getParentFile().getAbsolutePath());
            }
            
            os = new FileOutputStream(tmpFile);

            IOUtils.joinStreams(is, os);
        }
        catch (IOException e)
        {
            throw new PluginException(e);
        }
        finally
        {
            IOUtils.close(is);
            IOUtils.close(os);
        }

        if (!tmpFile.renameTo(downloadedFile))
        {
            tmpFile.delete();
            throw new PluginException("Unable to rename plugin temp file '" + tmpFile.getAbsolutePath() + "' to '" + downloadedFile.getAbsolutePath() + "'");
        }

        return downloadedFile.toURI();
    }

    private String deriveName(URI url)
    {
        String name = url.getPath();
        int index = name.lastIndexOf('/');
        if (index >= 0)
        {
            name = name.substring(index + 1);
        }
        return name;
    }

    public Plugin getPlugin(String id)
    {
        return findPlugin(id, plugins);
    }

    public Plugin getInternalPlugin(String id)
    {
        return findPlugin(id, internalPlugins);
    }

    private LocalPlugin findPlugin(final String id, List<LocalPlugin> plugins)
    {
        return CollectionUtils.find(plugins, new Predicate<LocalPlugin>()
        {
            public boolean satisfied(LocalPlugin plugin)
            {
                return plugin.getId().equals(id);
            }
        });
    }

    private List<LocalPlugin> sortPlugins(final List<LocalPlugin> plugins)
    {
        // A normal sort will not work as there is no ordering relationship
        // between plugins that have no dependency relationship.
        List<LocalPlugin> sorted = new LinkedList<LocalPlugin>();
        for (LocalPlugin plugin : plugins)
        {
            // Insert it as late as we can in sorted without inserting after
            // a transitive dependent.  If a dependent comes first, we are
            // sure to insert before it.  If it comes after, it will end up
            // after by virtue of being inserted as late as possible.
            int i;
            List<LocalPlugin> dependents = getDependentPlugins(plugin, plugins, true);
            for (i = 0; i < sorted.size(); i++)
            {
                if (dependents.contains(sorted.get(i)))
                {
                    break;
                }
            }

            sorted.add(i, plugin);
        }

        return sorted;
    }

    private List<LocalPlugin> getDependentPlugins(LocalPlugin plugin, List<LocalPlugin> plugins, boolean transitive)
    {
        List<LocalPlugin> result = new LinkedList<LocalPlugin>();
        addDependentPlugins(plugin, plugins, transitive, result);
        return result;
    }

    private void addDependentPlugins(LocalPlugin plugin, List<LocalPlugin> plugins, boolean transitive, List<LocalPlugin> result)
    {
        BundleDescription description = plugin.bundleDescription;
        if (description != null)
        {
            BundleDescription[] required = description.getDependents();
            if (required != null)
            {
                for (BundleDescription r : required)
                {
                    LocalPlugin p = findPlugin(r.getSymbolicName(), plugins);
                    if (p != null)
                    {
                        result.add(p);
                        if (transitive)
                        {
                            addDependentPlugins(p, plugins, transitive, result);
                        }
                    }
                }
            }
        }
    }

    public PluginRegistry getPluginRegistry()
    {
        return registry;
    }

    public IExtensionRegistry getExtensionRegistry()
    {
        return extensionRegistry;
    }

    public IExtensionTracker getExtensionTracker()
    {
        return extensionTracker;
    }

    public void registerExtensionManager(ExtensionManager extensionManager)
    {
        extensionManagers.add(extensionManager);
    }

    public void initialiseExtensions()
    {
        for (ExtensionManager extensionManager : extensionManagers)
        {
            ComponentContext.autowire(extensionManager);
            extensionManager.initialiseExtensions();
        }
    }

}
