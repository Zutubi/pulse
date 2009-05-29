package com.zutubi.pulse.core.plugins;

import com.zutubi.pulse.core.plugins.osgi.Equinox;
import com.zutubi.pulse.core.plugins.osgi.OSGiFramework;
import com.zutubi.pulse.core.spring.SpringComponentContext;
import com.zutubi.util.*;
import com.zutubi.util.io.IOUtils;
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

/**
 * The PluginManager is responsible for handling everything needed to support plugins.
 * <p/>
 * The Plugin Manager delegates much of the work to an embedded Equinox instance which
 * is responsible for handling the lifecycle, classloading and interactions between plugins.
 * <p/>
 * The Plugin Manager handles the management tasks around equinox, including managing the
 * deployed plugin files, tracking the plugin states (ENABLED, DISABLED etc), starting and
 * stopping the equinox installation, deploying new plugins at runtime, managing the upgrade
 * of plugins and more.
 */
public class PluginManager
{
    public static final boolean VERBOSE_EXTENSIONS = Boolean.getBoolean("pulse.verbose.extensions");

    private static final Logger LOG = Logger.getLogger(PluginManager.class);

    protected Equinox equinox;

    private PluginRegistry registry;

    private PluginPaths paths;

    /**
     * The list of non-internal plugins.
     */
    private List<LocalPlugin> plugins = new LinkedList<LocalPlugin>();

    // Special plugins that are loaded ahead of all others.  Upgrade handling does not apply to these plugins.
    // These plugins are also NOT registered in the registry.  They can not be disabled, uninstalled, upgraded
    // etc etc.
    private List<LocalPlugin> internalPlugins = new LinkedList<LocalPlugin>();

    private IExtensionRegistry extensionRegistry;
    private IExtensionTracker extensionTracker;

    //-- plugin registry keys. --

    private static final String PLUGIN_SOURCE_KEY = PluginRegistryEntry.PLUGIN_SOURCE_KEY;
    private static final String PLUGIN_PENDING_KEY = PluginRegistryEntry.PLUGIN_PENDING_KEY;
    private static final String UPGRADE_SOURCE_KEY = PluginRegistryEntry.UPGRADE_SOURCE_KEY;

    //--- pending action strings
    // There are some things that the plugin manager can not do whilst the equinox system
    // is running.  These actions are defined as pending actions, and will be carried out
    // on the next restart.
    /**
     * The plugin will be uninstalled on the next restart.
     */
    private static final String UNINSTALL_PENDING_ACTION = "uninstall";
    /**
     * The plugin will be disabled on the next restart.
     */
    private static final String DISABLE_PENDING_ACTION = "disable";
    /**
     * The plugin will be upgraded to a new version on the next restart.
     */
    private static final String UPGRADE_PENDING_ACTION = "upgrade";

    private static final PluginFileFilter PLUGIN_FILTER = new PluginFileFilter();

    private List<ExtensionManager> extensionManagers = new LinkedList<ExtensionManager>();

    private boolean versionChangeDetected = false;

    /**
     * The plugin states.
     */
    public enum State
    {
        /**
         * The plugin is deployed and running within the embedded equinox instance.
         */
        ENABLED,
        /**
         * The plugin is currently disabled.  It has not been registered with the equinox
         * system, but is located within the plugin storage directory, and as such can be
         * deployed.
         */
        DISABLED,
        /**
         * This plugin has been uninstalled.  It does not exist in the plugin storage
         * directory, and will not be automatically upgraded if it was one of the pre-packaged
         * plugins.
         */
        UNINSTALLED
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

        // extension registry is not available until the internal plugins containing the eclipse registry have
        // been loaded, that is, the internal plugins have been started up.
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

        for (String id : registry.getRegistrations())
        {
            PluginRegistryEntry entry = registry.getEntry(id);

            // initialise the installed plugins. Uninstalled plugins do not have source.
            if (entry.hasSource())
            {
                try
                {
                    LocalPlugin plugin = createPluginHandle(entry.getSource(), entry.getType());
                    plugin.setState(Plugin.State.INSTALLED);
                    State targetState = entry.getState();
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
        // - this processing is exactly the same as that done during the enable, except on in bulk
        versionChangeDetected = false;
        for (LocalPlugin plugin : installedPlugins)
        {
            PluginRegistryEntry entry = registry.getEntry(plugin.getId());
            if (entry.getVersion() != null)
            {
                PluginVersion registryVersion = entry.getVersion();
                if (registryVersion == null)
                {
                    LOG.warning("Unexpected null version string in plugin registry for " + plugin.getId() + ".");
                    continue;
                }
                // we should check for older versions here... can we go back?, and if we do, what happens to the
                // registry version.
                if (registryVersion.compareTo(plugin.getVersion()) != 0)
                {
                    plugin.setState(Plugin.State.VERSION_CHANGE);
                    versionChangeDetected = true;
                }
            }

            plugin.setBundle(equinox.install(plugin.getSource()));
            plugin.setBundleDescription(equinox.getBundleDescription(plugin.getId(), plugin.getVersion().toString()));
        }

        //NOTE: the extension works with resolved plugins, so we need to get a plugin to resolved before it can be upgraded. 

        // resolve them all.
        equinox.resolveBundles();

        //FIXME: Since we are resolving all of the bundles at once, we need to post process
        //FIXME: them and disable those that could not be resolved.
        //FIXME: This will also provide us with the opporunity to provide properly detailed error messages.

        List<LocalPlugin> resolvedPlugins = new LinkedList<LocalPlugin>();
        for (LocalPlugin plugin : installedPlugins)
        {
            switch (plugin.getBundle().getState())
            {
                case Bundle.INSTALLED:
                    // resolve failed.
                    try
                    {
                        plugin.getBundle().uninstall();
                        plugin.setBundle(null);
                        plugin.setBundleDescription(null);
                        plugin.setState(Plugin.State.DISABLED);
                        plugin.setErrorMessage("Failed to resolve bundle.");
                    }
                    catch (BundleException e)
                    {
                        //TODO: do we need to do more here?.
                        LOG.warning("Failed to uninstall bundle. Disabling plugin '" + plugin.getId() + "'. Cause: " + e.getMessage(), e);
                    }
                    break;
                case Bundle.RESOLVED:
                case Bundle.STARTING:
                    resolvedPlugins.add(plugin);
                    break;
                default:
                    LOG.warning("Unexpected plugin state; plugin: " + plugin.getName() + "(" + plugin.getId() + "), state: " + plugin.getBundle().getState());
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
                        plugin.getBundle().start(Bundle.START_TRANSIENT);
                        PluginRegistryEntry entry = registry.getEntry(plugin.getId());
                        entry.setVersion(plugin.getVersion());
                        saveRegistry();

                        plugin.setState(Plugin.State.ENABLED);
                    }
                    catch (BundleException e)
                    {
                        plugin.getBundle().uninstall();
                        plugin.setBundle(null);
                        plugin.setBundleDescription(null);
                        plugin.setState(Plugin.State.DISABLED);
                        plugin.setErrorMessage(e.getMessage());
                    }
                }
            }
        }
    }

    private void startupInternalPlugins() throws PluginException, BundleException
    {
        // A) load and install each of the plugins located in the internal storage location.
        for (File file : paths.getInternalPluginStorageDir().listFiles(PLUGIN_FILTER))
        {
            LocalPlugin internalPlugin = createPluginHandle(file, Plugin.Type.INTERNAL);
            internalPlugin.setBundle(equinox.install(internalPlugin.getSource()));
            internalPlugin.setBundleDescription(equinox.getBundleDescription(internalPlugin.getId(), internalPlugin.getVersion().toString()));
            internalPlugins.add(internalPlugin);
        }

        // B) Resolve all bundles, in this case all of the internal bundles.
        equinox.resolveBundles();

        // C) Start each of the internal plugins, ensuring that we do so in the order of there dependencies.
        internalPlugins = sortPlugins(internalPlugins);
        for (LocalPlugin plugin : internalPlugins)
        {
            plugin.getBundle().start(Bundle.START_TRANSIENT);
            plugin.setState(Plugin.State.ENABLED);
        }
    }

    public boolean isVersionChangeDetected()
    {
        return versionChangeDetected;
    }

    private void scanForManualUninstalls() throws PluginException
    {
        for (String id : registry.getRegistrations())
        {
            PluginRegistryEntry entry = registry.getEntry(id);

            // check for manually uninstalled plugins.
            if (entry.hasSource())
            {
                File source = getPluginSourceFile(entry.getSource());
                if (!source.exists())
                {
                    // looks like this plugin is no longer available.
                    entry.setState(State.UNINSTALLED);
                    entry.remove(PLUGIN_SOURCE_KEY);
                    saveRegistry();
                }
            }
        }
    }

    private void processPendingActions() throws PluginException
    {
        try
        {
            for (String id : registry.getRegistrations())
            {
                PluginRegistryEntry entry = registry.getEntry(id);
                try
                {
                    // process the pending actions.
                    if (entry.containsKey(PLUGIN_PENDING_KEY))
                    {
                        processPendingAction(id, entry);
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

    private void processPendingAction(String id, PluginRegistryEntry entry) throws PluginException, IOException, URISyntaxException
    {
        String pendingAction = entry.get(PLUGIN_PENDING_KEY);
        if (!TextUtils.stringSet(pendingAction))
        {
            LOG.warning("Registry entry for plugin '" + id + "' is corrupt. Missing pending action.");
            return;
        }

        if (pendingAction.equals(DISABLE_PENDING_ACTION))
        {
            entry.setState(State.DISABLED);
        }
        else if (pendingAction.equals(UNINSTALL_PENDING_ACTION))
        {
            if (entry.hasSource())
            {
                File plugin = getPluginSourceFile(entry.getSource());
                delete(plugin);
                entry.remove(PLUGIN_SOURCE_KEY);
            }
            entry.setState(State.UNINSTALLED);
        }
        else if (pendingAction.equals(UPGRADE_PENDING_ACTION))
        {
            URI newSource = getUpgradeSourceFile(entry.get(UPGRADE_SOURCE_KEY)).toURI();
            upgradePluginSource(id, newSource);

            // cleanup the temporary file.
            File tmpPluginFile = new File(newSource);
            delete(tmpPluginFile);

            entry.remove(UPGRADE_SOURCE_KEY);
        }
    }

    private void delete(File pluginSource) throws IOException
    {
        if (pluginSource.isDirectory())
        {
            FileSystemUtils.rmdir(pluginSource);
        }
        else if (pluginSource.isFile())
        {
            FileSystemUtils.delete(pluginSource);
        }
    }

    private void upgradePluginSource(String id, URI newSource) throws URISyntaxException, IOException, PluginException
    {
        // replace the old plugin, install and register the new source
        PluginRegistryEntry entry = registry.getEntry(id);

        // delete the old
        File pluginFile = getPluginSourceFile(entry.getSource());
        delete(pluginFile);

        // combination of uninstall the current plugin and installing the new.
        File installedSource = downloadPlugin(newSource, paths.getPluginStorageDir());

        // register the plugin with the registry
        entry.put(PLUGIN_SOURCE_KEY, installedSource.getName());
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
            LocalPlugin plugin = createPluginHandle(file, Plugin.Type.USER);
            try
            {
                if (!registry.isRegistered(plugin))
                {
                    // download and register - may want to make the default 'discovery' behaviour configurable?
                    File installedSource = downloadPlugin(file.toURI(), paths.getPluginStorageDir());

                    registerPlugin(createPluginHandle(installedSource, Plugin.Type.USER));
                }
                else
                {
                    PluginRegistryEntry entry = registry.getEntry(plugin.getId());

                    State state = entry.getState();
                    if (state == State.UNINSTALLED)
                    {
                        // this plugin has been uninstalled, nothing further is required from it.
                        continue;
                    }

                    // version check. if new version available, mark it for pending upgrade.
                    if (!entry.hasSource())
                    {
                        continue;
                    }

                    LocalPlugin registeredPlugin = createPluginHandle(entry.getSource(), entry.getType());

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
            LocalPlugin plugin = createPluginHandle(file, type);

            if (!registry.isRegistered(plugin))
            {
                // plugin is already where we want it, just a matter of installing it.
                registerPlugin(createPluginHandle(file, type));
            }
            else
            {
                PluginRegistryEntry entry = registry.getEntry(plugin.getId());

                if (State.UNINSTALLED.equals(entry.getState()))
                {
                    // plugin is already where we want it, just a matter of installing it.
                    registerPlugin(createPluginHandle(file, type));
                }
                else
                {
                    // is the current file the same as the registered file?
                    if (getPluginSourceFile(entry.getSource()).getName().compareTo(file.getName()) == 0)
                    {
                        continue;
                    }

                    // we always want to be using the latest version of the internal plugins, so update
                    // the registry with whatever we find. The system startup will detect the version change
                    // if one exists.
                    // NOTE: We are effectively re-registering each of the internal plugins each time round
                    entry.put(PLUGIN_SOURCE_KEY, file.getName());
                    saveRegistry();

                    // Alternative - only update if a) no version specified, b) there is a known version increase.
                }
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
        //TODO: check that the plugin we download does not already exist.  We do not want to install the same plugin twice.

        // copy it into the internal plugin storage directory
        File file = downloadPlugin(uri, filename, paths.getPluginStorageDir());

        LocalPlugin installedPlugin;
        try
        {
            if (!PLUGIN_FILTER.accept(file))
            {
                throw new PluginException("'" + uri + "' does not define a valid plugin.");
            }

            installedPlugin = createPluginHandle(file, Plugin.Type.USER);
            // register the plugin with the registry
            registerPlugin(installedPlugin);
        }
        catch (Exception e)
        {
            try
            {
                delete(file);
            }
            catch (IOException e1)
            {
                LOG.error(e1);
            }
            throw new PluginException(e);
        }

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
        PluginRegistryEntry entry = registry.getEntry(plugin.getId());
        entry.setState(State.DISABLED);
        saveRegistry();
        plugin.setState(Plugin.State.DISABLED);
    }

    void requestDisable(LocalPlugin plugin) throws PluginException
    {
        // the plugin is currently enabled, so we need to request a pending action.
        PluginRegistryEntry entry = registry.getEntry(plugin.getId());
        entry.put(PLUGIN_PENDING_KEY, DISABLE_PENDING_ACTION);
        saveRegistry();
        plugin.setState(Plugin.State.DISABLING);
    }

    void uninstallPlugin(LocalPlugin plugin) throws PluginException
    {
        try
        {
            PluginRegistryEntry entry = registry.getEntry(plugin.getId());
            if (entry.hasSource())
            {
                File pluginFile = getPluginSourceFile(entry.getSource());
                delete(pluginFile);

                entry.remove(PLUGIN_SOURCE_KEY);
            }
            entry.setState(State.UNINSTALLED);
            saveRegistry();

            plugin.setState(Plugin.State.UNINSTALLED);
        }
        catch (IOException e)
        {
            LOG.warning(e);
        }
    }

    void requestUninstall(LocalPlugin plugin) throws PluginException
    {
        // the plugin is currently enabled, so we need to request a pending action.
        PluginRegistryEntry entry = registry.getEntry(plugin.getId());
        entry.put(PLUGIN_PENDING_KEY, UNINSTALL_PENDING_ACTION);
        saveRegistry();

        plugin.setState(Plugin.State.UNINSTALLING);
    }

    void enablePlugin(LocalPlugin plugin) throws PluginException
    {
        // assumption: we can enable

        PluginRegistryEntry entry = registry.getEntry(plugin.getId());
        entry.setState(State.ENABLED);
        saveRegistry();

        // check version, if it has changed, then set the plugin state to VERSION_CHANGED.
        try
        {
            if (entry.getVersion() != null)
            {
                PluginVersion registryVersion = entry.getVersion();
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
                plugin.setBundle(equinox.resolve(plugin.getSource()));
                plugin.setBundleDescription(equinox.getBundleDescription(plugin.getId(), plugin.getVersion().toString()));
            }
            else
            {
                // fully activate the plugin.
                plugin.setBundle(equinox.activate(plugin.getSource()));
                plugin.setBundleDescription(equinox.getBundleDescription(plugin.getId(), plugin.getVersion().toString()));

                entry.setVersion(plugin.getVersion());
                saveRegistry();
                plugin.setState(Plugin.State.ENABLED);
            }
        }
        catch (Exception e)
        {
            try
            {
                if (plugin.getBundle() != null)
                {
                    plugin.getBundle().uninstall();
                }
            }
            catch (BundleException e1)
            {
                LOG.warning(e1);
            }

            plugin.setBundle(null);
            plugin.setBundleDescription(null);
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
            delete(pluginFile);

            plugins.remove(currentPlugin);

            // combination of uninstall the current plugin and installing the new.

            File installedSource = downloadPlugin(newSource, paths.getPluginStorageDir());

            LocalPlugin installedPlugin = createPluginHandle(installedSource, Plugin.Type.USER);

            // register the plugin with the registry
            PluginRegistryEntry registryEntry = registry.register(installedPlugin);
            registryEntry.put(PLUGIN_SOURCE_KEY, installedSource.getName());
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
            PluginRegistryEntry entry = registry.getEntry(plugin.getId());

            File pluginFile = getUpgradeSourceFile(entry.get(UPGRADE_SOURCE_KEY));
            delete(pluginFile);

            entry.remove(PLUGIN_PENDING_KEY);
            entry.remove(UPGRADE_SOURCE_KEY);
            saveRegistry();
            plugin.setState(Plugin.State.ENABLED);
        }
        catch (IOException e)
        {
            throw new PluginException(e);
        }
    }

    void requestUpgrade(LocalPlugin plugin, URI newSource) throws PluginException
    {
        File installedSource = downloadPlugin(newSource, paths.getPluginWorkDir());

        // the plugin is currently enabled, so we need to request a pending action.
        PluginRegistryEntry registryEntry = registry.getEntry(plugin.getId());
        registryEntry.put(PLUGIN_PENDING_KEY, UPGRADE_PENDING_ACTION);
        registryEntry.put(UPGRADE_SOURCE_KEY, installedSource.getName());
        saveRegistry();
        plugin.setState(Plugin.State.UPDATING);
    }

    void resolveVersionChange(LocalPlugin plugin)
    {
        plugin.setState(Plugin.State.INSTALLED);

        try
        {
            // fully activate the plugin.
            plugin.getBundle().start(Bundle.START_TRANSIENT);
            PluginRegistryEntry registryEntry = registry.getEntry(plugin.getId());
            registryEntry.setVersion(plugin.getVersion());
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
        return createPluginHandle(getPluginSourceFile(source), type);
    }

    private File getPluginSourceFile(String source) throws PluginException
    {
        if (isUriFormat(source))
        {
            return parseUriFormat(source);
        }
        else
        {
            return new File(paths.getPluginStorageDir(), source);
        }
    }

    private File getUpgradeSourceFile(String source) throws PluginException
    {
        if (isUriFormat(source))
        {
            return parseUriFormat(source);
        }
        else
        {
            return new File(paths.getPluginWorkDir(), source);
        }
    }

    /**
     * Provide backward compatibility support for absolute paths references were used for
     * source entries in the registry.
     *
     * @param source raw registry entry
     * @return the file representing the source string
     * @throws PluginException on error.
     */
    private File parseUriFormat(String source) throws PluginException
    {
        try
        {
            return new File(new URI(source));
        }
        catch (URISyntaxException e)
        {
            throw new PluginException(e);
        }
    }

    private boolean isUriFormat(String source)
    {
        return source.startsWith("file:/");
    }

    private LocalPlugin createPluginHandle(File file, Plugin.Type type) throws PluginException
    {
        if (!PLUGIN_FILTER.accept(file))
        {
            throw new PluginException("Unsupported plugin file format: " + file.getAbsolutePath() + ".");
        }

        LocalPlugin plugin;
        if (file.isDirectory())
        {
            plugin = new DirectoryPlugin(file);
        }
        else
        {
            plugin = new JarFilePlugin(file);
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

    private PluginRegistryEntry registerPlugin(LocalPlugin plugin) throws PluginException
    {
        try
        {
            PluginRegistryEntry registryEntry = registry.register(plugin);
            registryEntry.put(PLUGIN_SOURCE_KEY, new File(plugin.getSource()).getName());
            registryEntry.setState(State.ENABLED);
            registryEntry.setType(plugin.getType());
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
        List<Plugin> dependents = new LinkedList<Plugin>();
        if (plugin.getBundleDescription() != null)
        {
            dependents.addAll(getDependentPlugins(plugin, plugins, false));
        }
        return dependents;
    }

    public List<PluginDependency> getRequiredPlugins(LocalPlugin plugin)
    {
        BundleDescription description = plugin.getBundleDescription();
        if (description == null)
        {
            // It is the bundles description that tells us about the bundles requirements.
            // No description == no requirement information.  The plugin does have requirements,
            // but to get at them, we would need to read the raw bundle description outselves.
            return new LinkedList<PluginDependency>();
        }

        BundleSpecification[] requiredBundles = description.getRequiredBundles();
        return CollectionUtils.map(requiredBundles, new Mapping<BundleSpecification, PluginDependency>()
        {
            public PluginDependency map(BundleSpecification bundleSpecification)
            {
                return new PluginDependency(bundleSpecification.getName(),
                        convertVersionRange(bundleSpecification.getVersionRange()),
                        getPlugin(bundleSpecification.getName()));
            }
        });
    }

    private PluginVersionRange convertVersionRange(org.eclipse.osgi.service.resolver.VersionRange versionRange)
    {
        return new PluginVersionRange(convertVersion(versionRange.getMinimum()), versionRange.getIncludeMinimum(), convertVersion(versionRange.getMaximum()), versionRange.getIncludeMaximum());
    }

    private PluginVersion convertVersion(org.osgi.framework.Version version)
    {
        return new PluginVersion(version.getMajor(), version.getMinor(), version.getMicro(), version.getQualifier());
    }

    private File downloadPlugin(URI source, File dest) throws PluginException
    {
        return downloadPlugin(source, null, dest);
    }

    /**
     * Copy the source contents into the destination directory.
     *
     * @param source   the source to be downloaded.
     * @param filename the filename to use for the downloaded file.
     * @param dest     the directory into which the source will be downloaded.
     * @return the file reference to the downloaded plugin source.
     * @throws PluginException if a problem occurs with the copy.
     */
    private File downloadPlugin(URI source, String filename, File dest) throws PluginException
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

        if (!FileSystemUtils.rename(tmpFile, downloadedFile, true))
        {
            tmpFile.delete();
            throw new PluginException("Unable to rename plugin temp file '" + tmpFile.getAbsolutePath() + "' to '" + downloadedFile.getAbsolutePath() + "'");
        }

        return downloadedFile;
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

    /**
     * Sort the list of plugins according to there defined dependencies.  This sorting does not occur inplace.
     * <p/>
     * This only works for installed plugins as only these have the bundle descriptions available.
     *
     * @param plugins list to be sorted.
     * @return the sorted list of plugins.
     */
    private List<LocalPlugin> sortPlugins(final List<LocalPlugin> plugins)
    {
        List<LocalPlugin> unsortable = CollectionUtils.filter(plugins, new Predicate<LocalPlugin>()
        {
            public boolean satisfied(LocalPlugin plugin)
            {
                return plugin.getBundleDescription() == null;
            }
        });
        if (unsortable.size() > 0)
        {
            // we have plugins with no bundle descriptions.  These may be sorted as though they have no
            // dependencies, but is that correct?.  The problem here is really that all plugins should
            // already have there bundle descriptions.  If not, they will need to be installed.
            throw new IllegalStateException("Can not sort plugins that have not been installed.");
        }

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
        BundleDescription description = plugin.getBundleDescription();
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
            SpringComponentContext.autowire(extensionManager);
            extensionManager.initialiseExtensions();
        }
    }
}
