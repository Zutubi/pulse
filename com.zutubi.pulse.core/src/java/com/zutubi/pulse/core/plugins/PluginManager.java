package com.zutubi.pulse.core.plugins;

import com.zutubi.pulse.core.plugins.osgi.Equinox;
import com.zutubi.pulse.core.plugins.osgi.OSGiFramework;
import com.zutubi.pulse.core.plugins.util.DependencySort;
import com.zutubi.pulse.core.plugins.util.PluginFileFilter;
import com.zutubi.pulse.core.spring.SpringComponentContext;
import com.zutubi.tove.type.record.PathUtils;
import com.zutubi.util.*;
import com.zutubi.util.io.FileSystemUtils;
import com.zutubi.util.io.IOUtils;
import com.zutubi.util.logging.Logger;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.dynamichelpers.IExtensionTracker;
import org.eclipse.core.runtime.jobs.IJobManager;
import org.eclipse.osgi.service.resolver.BundleDescription;
import org.eclipse.osgi.service.resolver.BundleSpecification;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

/**
 * The PluginManager is responsible for handling everything needed to support plugins.
 * <p/>
 * The Plugin Manager delegates much of the work to an embedded Equinox instance which
 * handles the lifecycle, classloading and interactions between plugins.
 * <p/>
 * The Plugin Manager handles the management tasks around equinox, including managing the
 * deployed plugin files, tracking the plugin states (ENABLED, DISABLED etc), starting and
 * stopping equinox, deploying new plugins at runtime, managing the upgrade of plugins and
 * more.
 */
public class PluginManager
{
    public static final boolean VERBOSE_EXTENSIONS = Boolean.getBoolean("pulse.verbose.extensions");

    private static final Logger LOG = Logger.getLogger(PluginManager.class);

    //--- pending action strings
    // There are some things that the plugin manager can not do whilst the equinox system
    // is running.  These actions are defined as pending actions, and will be carried out
    // on the next restart.
    /**
     * The plugin will be uninstalled on the next restart.
     */
    private static final String PENDING_ACTION_UNINSTALL = "uninstall";
    /**
     * The plugin will be disabled on the next restart.
     */
    private static final String PENDING_ACTION_DISABLE = "disable";
    /**
     * The plugin will be upgraded to a new version on the next restart.
     */
    private static final String PENDING_ACTION_UPGRADE = "upgrade";


    private static final PluginFileFilter PLUGIN_FILTER = new PluginFileFilter();


    Equinox equinox;
    private PluginRegistry registry;
    private PluginPaths paths;
    private List<LocalPlugin> plugins = new LinkedList<LocalPlugin>();
    private List<ExtensionManager> extensionManagers = new LinkedList<ExtensionManager>();


    public synchronized void init() throws Exception
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
        equinox.setProperty(OSGiFramework.OSGI_CONFIGURATION_AREA, paths.getOsgiConfigurationDir().getAbsolutePath());
        equinox.setProperty(OSGiFramework.OSGI_CONFIGURATION_AREA_READONLY, Boolean.TRUE.toString());
        equinox.start(paths.getInternalPluginStorageDir());

        // Step 3: scan the various directories and update the registry accordingly.
        //  - register new plugins, mark uninstalls as uninstalled.
        //  - install the pre-packaged plugins, unless they have been explicitly marked as uninstalled.

        // Check for new plugins in the user directory before checking for
        // manual uninstalls so that manual upgrades are not misinterpreted.
        scanUserPlugins();
        scanForManualUninstalls();
        scanPrepackagedPlugins();

        // Step 4: plugin startup
        //  - install and resolve the plugins. (deal with unresolved plugins - provide as much feedback as possible).
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
                    LocalPlugin plugin = createPluginHandle(entry.getSource());
                    PluginRegistryEntry.Mode targetMode = entry.getMode();
                    switch (targetMode)
                    {
                        case ENABLE:
                            installedPlugins.add(plugin);
                            break;
                        case DISABLE:
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

        enableAll(installedPlugins);
    }

    private void scanForManualUninstalls() throws PluginException
    {
        for (String id : registry.getRegistrations())
        {
            PluginRegistryEntry entry = registry.getEntry(id);

            if (entry.hasSource())
            {
                File source = getPluginSourceFile(entry.getSource());
                if (!source.exists())
                {
                    // looks like this plugin is no longer available.
                    entry.setMode(PluginRegistryEntry.Mode.UNINSTALLED);
                    entry.removeSource();
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
                    if (entry.hasPendingAction())
                    {
                        processPendingAction(id, entry);
                        entry.removePendingAction();
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
        String pendingAction = entry.getPendingAction();
        if (!StringUtils.stringSet(pendingAction))
        {
            LOG.warning("Registry entry for plugin '" + id + "' is corrupt. Missing pending action.");
            return;
        }

        if (pendingAction.equals(PENDING_ACTION_DISABLE))
        {
            entry.setMode(PluginRegistryEntry.Mode.DISABLE);
        }
        else if (pendingAction.equals(PENDING_ACTION_UNINSTALL))
        {
            if (entry.hasSource())
            {
                File plugin = getPluginSourceFile(entry.getSource());
                delete(plugin, false);
                entry.removeSource();
            }
            entry.setMode(PluginRegistryEntry.Mode.UNINSTALLED);
        }
        else if (pendingAction.equals(PENDING_ACTION_UPGRADE))
        {
            URI newSource = getUpgradeSourceFile(entry.getUpgradeSource()).toURI();
            if (upgradePluginSource(id, newSource))
            {
                delete(new File(newSource), false);
                entry.removeUpgradeSource();
            }
        }
    }

    private boolean delete(File file, boolean throwOnError) throws PluginException
    {
        try
        {
            FileSystemUtils.delete(file);
            return true;
        }
        catch (IOException e)
        {
            if (throwOnError)
            {
                throw new PluginException(e);
            }
            else
            {
                LOG.warning(e);
            }
            
            return false;
        }
    }

    private boolean upgradePluginSource(String id, URI newSource) throws PluginException
    {
        PluginRegistryEntry entry = registry.getEntry(id);
        File pluginFile = getPluginSourceFile(entry.getSource());
        if (!delete(pluginFile, false))
        {
            return false;
        }

        File installedSource = downloadPlugin(newSource, paths.getPluginStorageDir());
        entry.setSource(installedSource.getName());
        saveRegistry();
        return true;
    }

    private void scanUserPlugins() throws PluginException
    {
        File storageDir = paths.getPluginStorageDir();
        if (!storageDir.exists() && !storageDir.mkdirs())
        {
            throw new PluginException("Cannot create plugin storage directory '" + storageDir.getAbsolutePath() + "'");
        }

        for (File file : storageDir.listFiles(PLUGIN_FILTER))
        {
            LocalPlugin plugin = createPluginHandle(file);

            if (!registry.isRegistered(plugin))
            {
                // plugin is already where we want it, just a matter of installing it.
                registerPlugin(createPluginHandle(file));
            }
            else
            {
                PluginRegistryEntry entry = registry.getEntry(plugin.getId());

                if (entry.getMode() == PluginRegistryEntry.Mode.UNINSTALLED)
                {
                    // plugin is already where we want it, just a matter of installing it.
                    registerPlugin(createPluginHandle(file));
                }
                else
                {
                    if (getPluginSourceFile(entry.getSource()).getName().compareTo(file.getName()) == 0)
                    {
                        continue;
                    }

                    File registeredSource = getPluginSourceFile(entry.getSource());
                    if (registeredSource.exists())
                    {
                        LOG.warning("Two versions of plugin '" + plugin.getId() + "' found: '" + registeredSource.getAbsolutePath() + "' and '" + file.getAbsolutePath() + "'.  Selecting the newer version.");
                        LocalPlugin registeredPlugin = createPluginHandle(registeredSource);
                        if (plugin.getVersion().compareTo(registeredPlugin.getVersion()) < 0)
                        {
                            continue;
                        }
                    }
                    
                    // Sync the registry to the manual change found on disk.
                    entry.setSource(file.getName());
                    saveRegistry();
                }
            }
        }
    }

    private void scanPrepackagedPlugins() throws PluginException
    {
        File prepackagedDir = paths.getPrepackagedPluginStorageDir();
        if (prepackagedDir == null || !prepackagedDir.isDirectory())
        {
            // During dev, the prepackaged plugins are compiled directly into the storage directory.
            // Lets just work with this for now.
            return;
        }

        for (File file : prepackagedDir.listFiles(PLUGIN_FILTER))
        {
            LocalPlugin plugin = createPluginHandle(file);
            if (!registry.isRegistered(plugin))
            {
                File downloadedSource = downloadPlugin(file.toURI(), file.getName(), paths.getPluginStorageDir());
                registerPlugin(createPluginHandle(downloadedSource));
            }
            else
            {
                PluginRegistryEntry entry = registry.getEntry(plugin.getId());

                PluginRegistryEntry.Mode mode = entry.getMode();
                if (mode == PluginRegistryEntry.Mode.UNINSTALLED || !entry.hasSource())
                {
                    // this plugin has been uninstalled, nothing further is required from it.
                    continue;
                }

                // version check. if new version available, mark it for pending upgrade.
                LocalPlugin registeredPlugin = createPluginHandle(entry.getSource());
                if (plugin.getVersion().compareTo(registeredPlugin.getVersion()) > 0)
                {
                    upgradePluginSource(plugin.getId(), file.toURI());
                }
            }
        }
    }

    public synchronized Plugin install(URI uri) throws PluginException
    {
        return install(uri, null);
    }

    public synchronized Plugin install(URI uri, String filename) throws PluginException
    {
        LocalPlugin plugin = downloadAndAdd(uri, filename);
        enablePlugin(plugin);
        return plugin;
    }
    
    public synchronized Plugin requestInstall(URI uri) throws PluginException
    {
        LocalPlugin plugin = downloadAndAdd(uri, null);
        plugin.setState(Plugin.State.INSTALLING);
        return plugin;
    }

    public synchronized List<? extends Plugin> installAll(List<URI> uris) throws PluginException
    {
        List<LocalPlugin> plugins = new LinkedList<LocalPlugin>();
        for (URI uri: uris)
        {
            plugins.add(downloadAndAdd(uri, null));
        }
        
        enableAll(plugins);
        return plugins;
    }

    private LocalPlugin downloadAndAdd(URI uri, String filename) throws PluginException
    {
        File file = downloadPlugin(uri, filename, paths.getPluginStorageDir());
        LocalPlugin installedPlugin;
        try
        {
            installedPlugin = createPluginHandle(file);
            if (getPlugin(installedPlugin.getId()) != null)
            {
                throw new PluginException("A plugin with id '" + installedPlugin.getId() + "' is already installed");
            }

            registerPlugin(installedPlugin);
        }
        catch (Exception e)
        {
            delete(file, false);
            throw new PluginException(e);
        }

        plugins.add(installedPlugin);
        return installedPlugin;
    }
    
    synchronized void disablePlugin(LocalPlugin plugin) throws PluginException
    {
        PluginRegistryEntry entry = registry.getEntry(plugin.getId());
        entry.setMode(PluginRegistryEntry.Mode.DISABLE);
        saveRegistry();
        plugin.setState(Plugin.State.DISABLED);
    }

    synchronized void requestDisable(LocalPlugin plugin) throws PluginException
    {
        // the plugin is currently enabled, so we need to request a pending action.
        PluginRegistryEntry entry = registry.getEntry(plugin.getId());
        entry.setPendingAction(PENDING_ACTION_DISABLE);
        saveRegistry();
        plugin.setState(Plugin.State.DISABLING);
    }

    synchronized void uninstallPlugin(LocalPlugin plugin) throws PluginException
    {
        PluginRegistryEntry entry = registry.getEntry(plugin.getId());
        if (entry.hasSource())
        {
            File pluginFile = getPluginSourceFile(entry.getSource());
            delete(pluginFile, false);

            entry.removeSource();
        }
        entry.setMode(PluginRegistryEntry.Mode.UNINSTALLED);
        saveRegistry();

        plugins.remove(plugin);
    }

    synchronized void requestUninstall(LocalPlugin plugin) throws PluginException
    {
        // the plugin is currently enabled, so we need to request a pending action.
        PluginRegistryEntry entry = registry.getEntry(plugin.getId());
        entry.setPendingAction(PENDING_ACTION_UNINSTALL);
        saveRegistry();

        plugin.setState(Plugin.State.UNINSTALLING);
    }

    synchronized void enablePlugin(LocalPlugin plugin) throws PluginException
    {
        PluginRegistryEntry entry = registry.getEntry(plugin.getId());
        entry.setMode(PluginRegistryEntry.Mode.ENABLE);
        saveRegistry();

        // activate this plugin within osgi.
        try
        {
            plugin.associateBundle(equinox.activate(plugin.getSource()), equinox.getBundleDescription(plugin.getId(), plugin.getVersion().toString()));
            plugin.setState(Plugin.State.ENABLED);
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

            plugin.disassociateBundle(e.getMessage());
        }
    }

    private void enableAll(List<LocalPlugin> plugins)
    {
        List<Bundle> bundles = new LinkedList<Bundle>();
        for (LocalPlugin plugin : plugins)
        {
            try
            {
                Bundle bundle = equinox.install(plugin.getSource());
                bundles.add(bundle);
                plugin.associateBundle(bundle, equinox.getBundleDescription(plugin.getId(), plugin.getVersion().toString()));
            }
            catch (BundleException e)
            {
                plugin.disassociateBundle("Unable to install bundle for plugin: " + e.getMessage());
            }
        }

        equinox.resolveBundles(bundles.toArray(new Bundle[bundles.size()]));

        List<LocalPlugin> resolvedPlugins = new LinkedList<LocalPlugin>();
        for (LocalPlugin plugin : plugins)
        {
            Bundle bundle = plugin.getBundle();
            if (bundle != null)
            {
                switch (bundle.getState())
                {
                    case Bundle.INSTALLED:
                        // resolve failed - can we get any more details about this?
                        safeUninstallBundle(plugin);
                        plugin.disassociateBundle("Failed to resolve bundle.");
                        break;
                    case Bundle.RESOLVED:
                    case Bundle.STARTING:
                    case Bundle.ACTIVE:
                        resolvedPlugins.add(plugin);
                        break;
                    default:
                        LOG.warning("Unexpected plugin state; plugin: " + plugin.getName() + "(" + plugin.getId() + "), state: " + bundle.getState());
                }
            }
        }

        List<LocalPlugin> sortedPlugins = sortPlugins(resolvedPlugins);
        for (LocalPlugin plugin : sortedPlugins)
        {
            try
            {
                plugin.getBundle().start(Bundle.START_TRANSIENT);
                plugin.setState(Plugin.State.ENABLED);
            }
            catch (BundleException e)
            {
                safeUninstallBundle(plugin);
                plugin.disassociateBundle(e.getMessage());
            }
        }
    }

    private void safeUninstallBundle(LocalPlugin plugin)
    {
        try
        {
            plugin.getBundle().uninstall();
        }
        catch (BundleException e)
        {
            LOG.warning("Failed to uninstall bundle for plugin '" + plugin.getId() + "': " + e.getMessage(), e);
        }
    }

    synchronized Plugin upgradePlugin(LocalPlugin currentPlugin, URI newSource) throws PluginException
    {
        // replace the old plugin, install and register the new source
        File pluginFile = new File(currentPlugin.getSource());
        delete(pluginFile, true);

        plugins.remove(currentPlugin);

        // combination of uninstall the current plugin and installing the new.

        File installedSource = downloadPlugin(newSource, paths.getPluginStorageDir());

        LocalPlugin installedPlugin = createPluginHandle(installedSource);

        // register the plugin with the registry
        PluginRegistryEntry registryEntry = registry.register(installedPlugin);
        registryEntry.setSource(installedSource.getName());
        saveRegistry();

        plugins.add(installedPlugin);
        installedPlugin.setState(currentPlugin.getState());

        return installedPlugin;
    }

    synchronized void cancelUpgrade(LocalPlugin plugin) throws PluginException
    {
        PluginRegistryEntry entry = registry.getEntry(plugin.getId());

        File pluginFile = getUpgradeSourceFile(entry.getUpgradeSource());
        delete(pluginFile, true);

        entry.removePendingAction();
        entry.removeUpgradeSource();
        saveRegistry();
        plugin.setState(Plugin.State.ENABLED);
    }

    synchronized void requestUpgrade(LocalPlugin plugin, URI newSource) throws PluginException
    {
        File installedSource = downloadPlugin(newSource, paths.getPluginWorkDir());

        // the plugin is currently enabled, so we need to request a pending action.
        PluginRegistryEntry registryEntry = registry.getEntry(plugin.getId());
        registryEntry.setPendingAction(PENDING_ACTION_UPGRADE);
        registryEntry.setUpgradeSource(installedSource.getName());
        saveRegistry();
        plugin.setState(Plugin.State.UPGRADING);
    }

    private LocalPlugin createPluginHandle(String source) throws PluginException
    {
        return createPluginHandle(getPluginSourceFile(source));
    }

    private File getPluginSourceFile(String source) throws PluginException
    {
        return getSourceFile(source, paths.getPluginStorageDir());
    }

    private File getUpgradeSourceFile(String source) throws PluginException
    {
        return getSourceFile(source, paths.getPluginWorkDir());
    }

    private File getSourceFile(String source, File baseDir) throws PluginException
    {
        if (isUriFormat(source))
        {
            return parseUriFormat(source);
        }
        else
        {
            return new File(baseDir, source);
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

    private LocalPlugin createPluginHandle(File file) throws PluginException
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

    private PluginRegistryEntry registerPlugin(LocalPlugin plugin) throws PluginException
    {
        try
        {
            PluginRegistryEntry registryEntry = registry.register(plugin);
            registryEntry.setSource(new File(plugin.getSource()).getName());
            registryEntry.setMode(PluginRegistryEntry.Mode.ENABLE);
            saveRegistry();
            return registryEntry;
        }
        catch (PluginException e)
        {
            registry.unregister(plugin.getId());
            throw e;
        }
    }

    public synchronized void destroy() throws Exception
    {
        equinox.stop();
    }

    public synchronized void setPluginPaths(PluginPaths paths)
    {
        this.paths = paths;
    }

    public synchronized List<Plugin> getPlugins()
    {
        return new LinkedList<Plugin>(plugins);
    }

    public synchronized List<Plugin> getDependentPlugins(LocalPlugin plugin)
    {
        List<Plugin> dependents = new LinkedList<Plugin>();
        if (plugin.getBundleDescription() != null)
        {
            dependents.addAll(getDependentPlugins(plugin, plugins));
        }
        return dependents;
    }

    public synchronized List<PluginDependency> getRequiredPlugins(LocalPlugin plugin)
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

        if (!dest.exists() && !dest.mkdirs())
        {
            throw new PluginException("Can not download plugin.  Failed to create destination: " + dest.getAbsolutePath());
        }

        File downloadedFile = new File(dest, filename);

        if (downloadedFile.exists())
        {
            throw new PluginException("Can not download plugin.  Plugin with name " + downloadedFile.getName() + " already exists.");
        }

        try
        {
            // treat local URIs differently because they may be either files or expanded directories.
            if ("file".equals(source.getScheme()))
            {
                downloadLocalTo(source, downloadedFile);
            }
            else
            {
                downloadRemoteTo(source, downloadedFile);
            }
        }
        catch (IOException e)
        {
            throw new PluginException(e);
        }

        return downloadedFile;
    }

    private void downloadLocalTo(URI source, File dest) throws IOException
    {
        FileSystemUtils.copy(dest, new File(source));
    }

    private void downloadRemoteTo(URI source, File dest) throws IOException
    {
        // ensure that we download all or nothing by downloading to a temporary location first.
        File tmpFile = new File(dest.getAbsolutePath() + ".tmp");

        try
        {
            if (!tmpFile.getParentFile().exists() && !tmpFile.getParentFile().mkdirs())
            {
                throw new IOException("Failed to download plugin. Unable to create new directory: " + tmpFile.getParentFile().getAbsolutePath());
            }

            IOUtils.joinStreams(source.toURL().openStream(), new FileOutputStream(tmpFile), true);

            // 'commit' the downloaded file by renaming it
            FileSystemUtils.rename(tmpFile, dest, true);
        }
        finally
        {
            if (tmpFile.exists() && !tmpFile.delete())
            {
                LOG.warning("Failed to delete: " + tmpFile.getAbsolutePath());
            }
        }
    }

    private String deriveName(URI uri)
    {
        String path = PathUtils.normalisePath(uri.getPath());
        return PathUtils.getBaseName(path);
    }

    public synchronized Plugin getPlugin(String id)
    {
        return findPlugin(id, plugins);
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
     * Sorts the given extensions based on the dependencies between their contributing plugins.
     * An extension from a dependent plugin is guaranteed to come after an extension from its
     * required plugin in the returned list.
     *
     * @param extensions the extensions to sort
     * @return the given extensions sorted by plugin dependencies
     */
    public synchronized List<IExtension> sortExtensions(IExtension[] extensions)
    {
        final Map<Plugin, List<IExtension>> extensionsByPlugin = new HashMap<Plugin, List<IExtension>>();
        for (IExtension extension: extensions)
        {
            Plugin contributingPlugin = getPlugin(extension.getContributor().getName());
            List<IExtension> byPlugin = extensionsByPlugin.get(contributingPlugin);
            if (byPlugin == null)
            {
                byPlugin = new LinkedList<IExtension>();
                extensionsByPlugin.put(contributingPlugin, byPlugin);
            }

            byPlugin.add(extension);
        }

        return DependencySort.sort(Arrays.asList(extensions), new UnaryFunction<IExtension, Set<IExtension>>()
        {
            public Set<IExtension> process(IExtension extension)
            {
                LocalPlugin contributingPlugin = (LocalPlugin) getPlugin(extension.getContributor().getName());
                List<Plugin> dependentPlugins = getDependentPlugins(contributingPlugin);
                Set<IExtension> dependentExtensions = new HashSet<IExtension>();
                for (Plugin dependentPlugin: dependentPlugins)
                {
                    List<IExtension> byPlugin = extensionsByPlugin.get(dependentPlugin);
                    if (byPlugin != null)
                    {
                        dependentExtensions.addAll(byPlugin);
                    }
                }
                
                return dependentExtensions;
            }
        });
    }

    /**
     * Sort the list of plugins according to their defined dependencies.  This sorting does not occur inplace.
     * <p/>
     * This only works for installed plugins as only these have the bundle descriptions available.
     *
     * @param plugins list to be sorted.
     * @return the sorted list of plugins.
     */
    private List<LocalPlugin> sortPlugins(final List<LocalPlugin> plugins)
    {
        boolean unsortablePlugin = CollectionUtils.contains(plugins, new Predicate<LocalPlugin>()
        {
            public boolean satisfied(LocalPlugin plugin)
            {
                return plugin.getBundleDescription() == null;
            }
        });

        if (unsortablePlugin)
        {
            throw new IllegalStateException("Can not sort plugins that have not been installed.");
        }

        return DependencySort.sort(plugins, new UnaryFunction<LocalPlugin, Set<LocalPlugin>>()
        {
            public Set<LocalPlugin> process(LocalPlugin plugin)
            {
                return getDependentPlugins(plugin, plugins);
            }
        });
    }

    private Set<LocalPlugin> getDependentPlugins(LocalPlugin plugin, List<LocalPlugin> plugins)
    {
        Set<LocalPlugin> result = new HashSet<LocalPlugin>();
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
                }
            }
        }
        
        return result;
    }

    public synchronized PluginRegistry getPluginRegistry()
    {
        return registry;
    }

    public synchronized IExtensionRegistry getExtensionRegistry()
    {
        return equinox.getExtensionRegistry();
    }

    public synchronized IExtensionTracker getExtensionTracker()
    {
        return equinox.getExtensionTracker();
    }

    public synchronized IJobManager getJobManager()
    {
        return equinox.getJobManager();
    }

    public synchronized void registerExtensionManager(ExtensionManager extensionManager)
    {
        extensionManagers.add(extensionManager);
    }

    public synchronized void initialiseExtensions()
    {
        for (ExtensionManager extensionManager : extensionManagers)
        {
            SpringComponentContext.autowire(extensionManager);
            extensionManager.initialiseExtensions();
        }
    }
}
