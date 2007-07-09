package com.zutubi.pulse.plugins;

import com.zutubi.pulse.bootstrap.ComponentContext;
import com.zutubi.pulse.util.FileSystemUtils;
import com.zutubi.util.*;
import com.zutubi.util.logging.Logger;
import nu.xom.*;
import org.eclipse.core.internal.registry.osgi.OSGIUtils;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.RegistryFactory;
import org.eclipse.core.runtime.adaptor.EclipseStarter;
import org.eclipse.core.runtime.dynamichelpers.ExtensionTracker;
import org.eclipse.core.runtime.dynamichelpers.IExtensionTracker;
import org.eclipse.osgi.framework.internal.core.FrameworkProperties;
import org.eclipse.osgi.framework.util.Headers;
import org.eclipse.osgi.service.resolver.BundleDescription;
import org.eclipse.osgi.service.resolver.BundleSpecification;
import org.eclipse.osgi.service.resolver.PlatformAdmin;
import org.eclipse.osgi.service.resolver.State;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.packageadmin.PackageAdmin;

import java.io.*;
import java.net.URL;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class DefaultPluginManager implements PluginManager
{
    private static final Logger LOG = Logger.getLogger(DefaultPluginManager.class);

    private static final String ELEMENT_PLUGINS = "plugins";
    private static final String ELEMENT_PLUGIN = "plugin";
    private static final String ATTRIBUTE_ID = "id";
    private static final String ATTRIBUTE_NAME = "name";
    private static final String ATTRIBUTE_FILE = "file";
    private static final String ATTRIBUTE_STATE = "state";

    private PluginPaths pluginPaths;
    private BundleContext context;
    private IExtensionRegistry extensionRegistry;
    private ServiceReference packageAdminRef;
    private PackageAdmin packageAdmin;
    private ServiceReference platformAdminRef;
    private PlatformAdmin platformAdmin;
    private State offlineState;
    private int offlineId = 1;

    private IExtensionTracker extensionTracker;
    private List<PluginImpl> plugins;
    private List<ExtensionManager> extensionManagers = new LinkedList<ExtensionManager>();

    public DefaultPluginManager()
    {
    }

    public void init()
    {
        plugins = Collections.synchronizedList(new LinkedList<PluginImpl>());
        System.setProperty("osgi.configuration.area", pluginPaths.getPluginConfigurationRoot().getAbsolutePath());

        LOG.info("Starting plugin manager...");
        try
        {
            context = EclipseStarter.startup(new String[] { "-clean" }, null);

            packageAdminRef = context.getServiceReference(PackageAdmin.class.getName());
            if (packageAdminRef != null)
            {
                packageAdmin = (PackageAdmin) context.getService(packageAdminRef);
            }

            if (packageAdmin == null)
            {
                LOG.severe("Could not access package admin service");
                return;
            }

            platformAdminRef = context.getServiceReference(PlatformAdmin.class.getName());
            if (platformAdminRef != null)
            {
                platformAdmin = (PlatformAdmin) context.getService(platformAdminRef);
            }

            if (platformAdmin == null)
            {
                LOG.severe("Could not access platform admin service");
                return;
            }

            offlineState = platformAdmin.getFactory().createState(platformAdmin.getState());
            offlineState.setResolver(platformAdmin.getResolver());
            offlineState.setPlatformProperties(FrameworkProperties.getProperties());
            loadInternalPlugins();

            extensionRegistry = RegistryFactory.getRegistry();
            extensionTracker = new ExtensionTracker(extensionRegistry);

            loadPrepackagedPlugins();

            // Ensure we have a user plugins directory
            // FIXME
//            File userPlugins = pluginPaths.getUserPluginRoot();
//            if (!userPlugins.isDirectory())
//            {
//                userPlugins.mkdirs();
//            }
//
//            loadUserPlugins();

            LOG.info("Plugin manager started.");
        }
        catch (Exception e)
        {
            LOG.severe("Unable to start plugin manager: " + e.getMessage(), e);
        }
    }

    public void initialiseExtensions()
    {
        for(ExtensionManager extensionManager: extensionManagers)
        {
            // FIXME
            ComponentContext.autowire(extensionManager);
            extensionManager.initialiseExtensions();
        }
    }

    public void destroy()
    {
        try
        {
            if (packageAdminRef != null)
            {
                context.ungetService(packageAdminRef);
            }

            if (platformAdminRef != null)
            {
                context.ungetService(platformAdminRef);
            }

            EclipseStarter.shutdown();
        }
        catch (Exception e)
        {
            LOG.warning("Unable to shut down plugin manager: " + e.getMessage(), e);
        }
    }

    private void loadInternalPlugins()
    {
        LOG.info("Loading internal plugins...");
        loadPlugins(pluginPaths.getInternalPluginRoot(), PluginImpl.Type.INTERNAL);
        LOG.info("Internal plugins loaded.");
    }

    private void loadPrepackagedPlugins()
    {
        LOG.info("Loading pre-packaged plugins...");
        List<PluginImpl> foundPlugins = loadPlugins(pluginPaths.getPrepackagedPluginRoot(), PluginImpl.Type.PREPACKAGED);
        plugins.addAll(foundPlugins);
        LOG.info("Pre-packaged plugins loaded.");
    }

    private void loadUserPlugins()
    {
        LOG.info("Loading user plugins...");
        List<PluginImpl> foundPlugins = loadPlugins(pluginPaths.getUserPluginRoot(), PluginImpl.Type.USER);
        plugins.addAll(foundPlugins);
        LOG.info("User plugins loaded.");
    }

    private List<PluginImpl> loadPlugins(File pluginDir, PluginImpl.Type type)
    {
        List<PluginImpl> foundPlugins = new LinkedList<PluginImpl>();

        if (pluginDir.isDirectory())
        {
            // Load the plugins file, if it exists (if not it is the same as
            // an empty plugins file).
            loadPluginsFile(pluginDir, type, foundPlugins);

            // Dicover plugins in the directory not listed in the file.
            discoverPlugins(pluginDir, type, foundPlugins);

            // Need to resolve OSGi bundles before starting (otherwise
            // Equinox barfs).
            resolveBundles(null);

            foundPlugins = sortPlugins(foundPlugins);

            // Start enabled plugins
            for (PluginImpl plugin : foundPlugins)
            {
                if (plugin.isEnabled())
                {
                    startPlugin(plugin);
                }
            }

            // Write out a new plugins file.
            writePluginsFile(pluginDir, foundPlugins);
        }
        else
        {
            LOG.severe("Plugins directory '" + pluginDir.getAbsolutePath() + "' does not exist");
        }

        return foundPlugins;
    }

    private List<PluginImpl> sortPlugins(final List<PluginImpl> foundPlugins)
    {
        // A normal sort will not work as there is no ordering relationship
        // between plugins that have no dependency relationship.
        List<PluginImpl> sorted = new LinkedList<PluginImpl>();
        for(PluginImpl plugin: foundPlugins)
        {
            // Insert it as late as we can in sorted without inserting after
            // a transitive dependent.  If a dependent comes first, we are
            // sure to insert before it.  If it comes after, it will end up
            // after by virtue of being inserted as late as possible.
            int i;
            List<Plugin> dependents = getDependentPlugins(plugin, foundPlugins, true);
            for(i = 0; i < sorted.size(); i++)
            {
                if(dependents.contains(sorted.get(i)))
                {
                    break;
                }
            }

            sorted.add(i, plugin);
        }

        return sorted;
    }

    private void startPlugin(PluginImpl plugin)
    {
        Bundle bundle = plugin.getBundle();
        if (bundle.getState() == Bundle.INSTALLED)
        {
            // Resolve the bundle first
            resolveBundles(new Bundle[] { bundle });
        }

        LOG.info("Starting plugin " + bundle.getSymbolicName());
        try
        {
            bundle.start(Bundle.START_TRANSIENT);
        }
        catch (BundleException e)
        {
            // Setting the in memory state is sufficient as the
            // file is rewritten below.
            plugin.setState(Plugin.State.DISABLED);
            plugin.setErrorMessage("Unable to start plugin: " + e.getMessage() + " (see logs for trace)");
            LOG.warning("Unable to start plugin '" + plugin.getName() + "': " + e.getMessage(), e);
        }
    }

    private void loadPluginsFile(File pluginDir, PluginImpl.Type type, List<PluginImpl> foundPlugins)
    {
        Document doc = readPluginsFile(pluginDir);
        if (doc != null)
        {
            processPluginsDocument(pluginDir, doc, type, foundPlugins);
        }
    }

    private Document readPluginsFile(File pluginDir)
    {
        Document doc = null;
        File pluginsFile = new File(pluginDir, "plugins.xml");
        if (pluginsFile.exists())
        {
            FileInputStream input = null;
            try
            {
                input = new FileInputStream(pluginsFile);
                Builder builder = new Builder();
                doc = builder.build(input);
            }
            catch (ParsingException pex)
            {
                LOG.severe("Unable to parse plugins file '" + pluginsFile.getAbsolutePath() + "'", pex);
            }
            catch (IOException e)
            {
                LOG.severe("I/O error processing plugins file '" + pluginsFile.getAbsolutePath() + "'", e);
            }
            finally
            {
                IOUtils.close(input);
            }
        }

        return doc;
    }

    private void processPluginsDocument(File pluginDir, Document doc, PluginImpl.Type type, List<PluginImpl> foundPlugins)
    {
        Elements pluginElements = doc.getRootElement().getChildElements(ELEMENT_PLUGIN);
        for (int i = 0; i < pluginElements.size(); i++)
        {
            Element pluginElement = pluginElements.get(i);
            String name = pluginElement.getAttributeValue(ATTRIBUTE_NAME);
            String file = pluginElement.getAttributeValue(ATTRIBUTE_FILE);
            String stateString = pluginElement.getAttributeValue(ATTRIBUTE_STATE);

            if (name == null)
            {
                LOG.warning("Ignoring listed plugin with missing 'name' attribute");
                continue;
            }

            if (file == null)
            {
                LOG.warning("Ignoring listed plugin '" + name + "' with missing 'file' attribute");
                continue;
            }

            if (stateString == null)
            {
                LOG.warning("Ignoring listed plugin '" + name + "' with missing 'state' attribute");
                continue;
            }

            Plugin.State state;
            try
            {
                state = Plugin.State.valueOf(stateString.toUpperCase());
            }
            catch (IllegalArgumentException e)
            {
                LOG.warning("Ignoring listed plugin '" + name + "' with invalid 'state' attribute '" + stateString + "'");
                continue;
            }

            try
            {
                File pluginFile = new File(pluginDir, file);

                // If the file no longer exists, the plugin is just forgotten.
                if (pluginFile.exists())
                {
                    switch (state)
                    {
                        case DISABLED:
                        case DISABLING:
                            foundPlugins.add(loadPluginFile(pluginFile, type, false, Plugin.State.DISABLED));
                            break;
                        case ENABLED:
                            foundPlugins.add(loadPluginFile(pluginFile, type, false, Plugin.State.ENABLED));
                            break;
                        case UNINSTALLING:
                        case UPDATING:
                            if (pluginFile.isDirectory())
                            {
                                FileSystemUtils.rmdir(pluginFile);
                            }
                            else
                            {
                                pluginFile.delete();
                            }
                            break;
                    }
                }
            }
            catch (PluginException e)
            {
                LOG.warning("Unable to load plugin '" + name + "': " + e.getMessage());
            }
        }
    }

    private void discoverPlugins(File pluginDir, PluginImpl.Type type, List<PluginImpl> foundPlugins)
    {
        for (File pluginFile : pluginDir.listFiles())
        {
            if (!havePluginFromFile(pluginFile, foundPlugins) && isPlugin(pluginFile))
            {
                try
                {
                    PluginImpl plugin = loadPluginFile(pluginFile, type, false, Plugin.State.ENABLED);
                    foundPlugins.add(plugin);
                }
                catch (PluginException e)
                {
                    LOG.warning("Unable to load plugin from file '" + pluginFile.getAbsolutePath() + "': " + e.getMessage(), e);
                }
            }
        }
    }

    private boolean havePluginFromFile(File pluginFile, List<PluginImpl> foundPlugins)
    {
        for (PluginImpl plugin : foundPlugins)
        {
            if (plugin.getPluginFile().equals(pluginFile))
            {
                return true;
            }
        }

        return false;
    }

    private boolean isPlugin(File pluginFile)
    {
        if (pluginFile.isDirectory())
        {
            File manifest = new File(pluginFile, FileSystemUtils.composeFilename("META-INF", "MANIFEST.MF"));
            return manifest.exists();
        }
        else
        {
            return pluginFile.getName().toLowerCase().endsWith(".jar");
        }
    }

    private PluginImpl loadPluginFile(File pluginFile, PluginImpl.Type type, boolean update, Plugin.State state) throws PluginException
    {
        Headers manifest = loadBundleManifest(pluginFile);
        BundleDescription bundleDescription;

        try
        {
            bundleDescription = platformAdmin.getFactory().createBundleDescription(offlineState, manifest, getBundleLocation(pluginFile), offlineId++);
            offlineState.addBundle(bundleDescription);
            offlineState.resolve();
        }
        catch (BundleException e)
        {
            LOG.warning(e);
            throw new PluginException(e);
        }

        PluginImpl plugin = new PluginImpl(manifest, bundleDescription, pluginFile, state, type);
        if(!update)
        {
            if (getPlugin(bundleDescription.getSymbolicName()) != null)
            {
                throw new PluginException("A plugin with the same identifier (" + bundleDescription.getSymbolicName() + ") already exists.");
            }

            if(state == Plugin.State.ENABLED)
            {
                try
                {
                    Bundle bundle = installBundle(pluginFile);
                    plugin.setBundle(bundle);
                }
                catch (BundleException e)
                {
                    LOG.warning(e);
                    plugin.setState(Plugin.State.DISABLED);
                    plugin.setErrorMessage("Unable to install plugin: " + e.getMessage());
                }
            }
        }

        return plugin;
    }

    private Headers loadBundleManifest(File pluginFile) throws PluginException
    {
        try
        {
            Headers manifest;

            if (pluginFile.isDirectory())
            {
                InputStream manifestIn = new FileInputStream(new File(pluginFile, FileSystemUtils.composeFilename("META-INF", "MANIFEST.MF")));
                try
                {
                    manifest = Headers.parseManifest(manifestIn);
                }
                finally
                {
                    IOUtils.close(manifestIn);
                }
            }
            else
            {
                JarFile jarFile = null;
                try
                {
                    jarFile = new JarFile(pluginFile);
                    JarEntry entry = jarFile.getJarEntry("META-INF/MANIFEST.MF");
                    if(entry == null)
                    {
                        throw new PluginException("No manifest found");
                    }

                    InputStream manifestIn = jarFile.getInputStream(entry);
                    try
                    {
                        manifest = Headers.parseManifest(manifestIn);
                    }
                    finally
                    {
                        IOUtils.close(manifestIn);
                    }
                }
                finally
                {
                    IOUtils.close(jarFile);
                }
            }

            return manifest;
        }
        catch (IOException e)
        {
            throw new PluginException(e);
        }
        catch (BundleException e)
        {
            throw new PluginException(e);
        }
    }

    private Bundle installBundle(File pluginFile) throws BundleException
    {
        Bundle bundle = context.installBundle(getBundleLocation(pluginFile));
        if (bundle.getSymbolicName() == null)
        {
            bundle.uninstall();
            throw new BundleException("Bundle missing required header Bundle-SymbolicName");
        }

        return bundle;
    }

    private String getBundleLocation(File pluginFile)
    {
        return "reference:file:" + pluginFile.getAbsolutePath();
    }

    private void writePluginsFile(File pluginDir, List<PluginImpl> foundPlugins)
    {
        Element root = new Element(ELEMENT_PLUGINS);
        Document doc = new Document(root);
        for (PluginImpl plugin : foundPlugins)
        {
            addPluginElement(plugin, root);
        }

        writePluginsDoc(pluginDir, doc);
    }

    private void addPluginElement(PluginImpl plugin, Element root)
    {
        Element element = new Element(ELEMENT_PLUGIN);
        element.addAttribute(new Attribute(ATTRIBUTE_ID, plugin.getId()));
        element.addAttribute(new Attribute(ATTRIBUTE_NAME, plugin.getName()));
        element.addAttribute(new Attribute(ATTRIBUTE_FILE, plugin.getPluginFile().getName()));
        element.addAttribute(new Attribute(ATTRIBUTE_STATE, plugin.getState().toString().toLowerCase()));
        root.appendChild(element);
    }

    private void writePluginsDoc(File pluginDir, Document doc)
    {
        File pluginsFile = new File(pluginDir, "plugins.xml");
        BufferedOutputStream bos = null;
        try
        {
            FileOutputStream fos = new FileOutputStream(pluginsFile);
            bos = new BufferedOutputStream(fos);

            Serializer serializer = new Serializer(bos);
            serializer.write(doc);
        }
        catch (IOException e)
        {
            LOG.severe("Unable to save plugin list to file '" + pluginsFile.getAbsolutePath() + "': " + e.getMessage(), e);
        }
        finally
        {
            IOUtils.close(bos);
        }
    }

    private void resolveBundles(Bundle[] bundles)
    {
        if (bundles != null && bundles.length == 0)
        {
            return;
        }

        packageAdmin.resolveBundles(bundles);
    }

    public void registerExtensionManager(ExtensionManager extensionManager)
    {
        extensionManagers.add(extensionManager);
    }

    public IExtensionRegistry getExtenstionRegistry()
    {
        return extensionRegistry;
    }

    public IExtensionTracker getExtenstionTracker()
    {
        return extensionTracker;
    }

    public List<Plugin> getAllPlugins()
    {
        List<Plugin> result = new ArrayList<Plugin>(plugins.size());
        for (PluginImpl p : plugins)
        {
            result.add(p);
        }

        // Return in name order by default.
        final Comparator<String> nameComparator = new Sort.StringComparator();
        Collections.sort(result, new Comparator<Plugin>()
        {
            public int compare(Plugin o1, Plugin o2)
            {
                return nameComparator.compare(o1.getName(), o2.getName());
            }
        });
        return result;
    }

    public List<PluginRequirement> getRequiredPlugins(Plugin plugin)
    {
        final PluginImpl pluginImpl = (PluginImpl) plugin;
        BundleSpecification[] requiredBundles = pluginImpl.getBundleDescription().getRequiredBundles();
        return CollectionUtils.map(requiredBundles, new Mapping<BundleSpecification, PluginRequirement>()
        {
            public PluginRequirement map(BundleSpecification bundleSpecification)
            {
                return new PluginRequirement(bundleSpecification.getName(), convertVersionRange(bundleSpecification.getVersionRange()), getPlugin(bundleSpecification.getName()));
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

    public List<Plugin> getDependentPlugins(Plugin plugin)
    {
        return getDependentPlugins((PluginImpl) plugin, plugins, false);
    }

    private List<Plugin> getDependentPlugins(PluginImpl pluginImpl, List<PluginImpl> fromPlugins, boolean transitive)
    {
        List<Plugin> result = new LinkedList<Plugin>();
        addDependentPlugins(pluginImpl, fromPlugins, transitive, result);
        return result;
    }

    private void addDependentPlugins(PluginImpl pluginImpl, List<PluginImpl> fromPlugins, boolean transitive, List<Plugin> result)
    {
        BundleDescription[] required = pluginImpl.getBundleDescription().getDependents();
        if (required != null)
        {
            for (BundleDescription r : required)
            {
                PluginImpl p = findPlugin(r.getSymbolicName(), fromPlugins);
                if (p != null)
                {
                    result.add(p);
                    if (transitive)
                    {
                        addDependentPlugins(p, fromPlugins, transitive, result);
                    }
                }
            }
        }
    }

    public PluginImpl getPlugin(final String id)
    {
        return (PluginImpl) findPlugin(id, plugins);
    }

    private PluginImpl findPlugin(final String id, List<PluginImpl> plugins)
    {
        return CollectionUtils.find(plugins, new Predicate<PluginImpl>()
        {
            public boolean satisfied(PluginImpl plugin)
            {
                return plugin.getId().equals(id);
            }
        });
    }

    public Plugin installPlugin(URL url) throws PluginException
    {
        return installPlugin(deriveName(url), url);
    }

    String deriveName(URL url)
    {
        String name = url.getPath();
        int index = name.lastIndexOf('/');
        if (index >= 0)
        {
            name = name.substring(index + 1);
        }
        return name;
    }

    public Plugin installPlugin(String name, URL url) throws PluginException
    {
        // Load it up.  Errors before loading are thrown.  Afterwards, they
        // are recorded on the plugin itself.
        File pluginFile = copyInPlugin(name, url);
        PluginImpl plugin = loadPlugin(pluginFile, false);

        // And start it
        startPlugin(plugin);

        // Add the plugin to the list (in memory and file)
        plugins.add(plugin);
        addToPluginsFile(plugin);

        return plugin;
    }

    File copyInPlugin(String name, URL url) throws PluginException
    {
        File pluginFile = new File(pluginPaths.getUserPluginRoot(), name);
        if (pluginFile.exists())
        {
            throw new PluginException("Plugin file '" + pluginFile.getAbsolutePath() + "' already exists");
        }

        // Copy the contents of the given URL to a file with the given name
        // in the user plugins directory, then install the plugin from this
        // file.  To protect ourselves, we copy to a temp file and rename it
        // "atomically".
        File tmpFile = null;
        InputStream is = null;
        OutputStream os = null;

        try
        {
            is = url.openStream();

            tmpFile = new File(pluginFile.getAbsolutePath() + ".tmp");
            os = new FileOutputStream(tmpFile);

            IOUtils.joinStreams(is, os);
        }
        catch (IOException e)
        {
            throw new PluginException("I/O error copying plugin: " + e.getMessage(), e);
        }
        finally
        {
            IOUtils.close(is);
            IOUtils.close(os);
        }

        if (!tmpFile.renameTo(pluginFile))
        {
            tmpFile.delete();
            throw new PluginException("Unable to rename plugin temp file '" + tmpFile.getAbsolutePath() + "' to '" + pluginFile.getAbsolutePath() + "'");
        }

        return pluginFile;
    }

    private PluginImpl loadPlugin(File pluginFile, boolean update) throws PluginException
    {
        try
        {
            return loadPluginFile(pluginFile, PluginImpl.Type.USER, update, Plugin.State.ENABLED);
        }
        catch (PluginException e)
        {
            LOG.warning("Unable to load plugin from file '" + pluginFile.getAbsolutePath() + "': " + e.getMessage(), e);
            throw new PluginException("Unable to load plugin: " + e.getMessage(), e);
        }
    }

    private void addToPluginsFile(PluginImpl plugin)
    {
        File pluginDir = getPluginDir(plugin);
        Document doc = readPluginsFile(pluginDir);
        if (doc != null)
        {
            addPluginElement(plugin, doc.getRootElement());
            writePluginsDoc(pluginDir, doc);
        }
    }

    public void updatePlugin(Plugin plugin, URL url) throws PluginException
    {
        updatePlugin(plugin, deriveName(url), url);
    }

    public void updatePlugin(Plugin plugin, String name, URL url) throws PluginException
    {
        PluginImpl currentPlugin = (PluginImpl) plugin;
        if (currentPlugin.isInternal())
        {
            throw new PluginException("Cannot update an internal plugin");
        }

        switch (currentPlugin.getState())
        {
            case UNINSTALLING:
                throw new PluginException("Unable to update plugin: already marked for uninstall");
            case UPDATING:
                throw new PluginException("Unable to update plugin: already marked for update");
        }

        File pluginFile = copyInPlugin(name, url);
        PluginImpl newPlugin = loadPluginFile(pluginFile, PluginImpl.Type.USER, true, currentPlugin.isEnabled() ? Plugin.State.ENABLED : Plugin.State.DISABLED);
        addToPluginsFile(newPlugin);

        // Mark the old version for removal.
        changeState(currentPlugin, Plugin.State.UPDATING);
    }

    private void checkUpdateDependencies(PluginImpl currentPlugin, PluginImpl newPlugin) throws PluginException
    {
        try
        {
            // Now use the platform admin service to check dependents
            State state = platformAdmin.getState(false);
            long bundleId = currentPlugin.getBundle().getBundleId();
            BundleDescription currentDescription = state.getBundle(bundleId);
            BundleDescription newDescription = state.getBundle(newPlugin.getBundle().getBundleId());
            BundleDescription[] dependents = currentDescription.getDependents();
            for (BundleDescription dependent : dependents)
            {
                BundleSpecification[] requirements = dependent.getRequiredBundles();
                for (BundleSpecification requirement : requirements)
                {
                    if (requirement.getSupplier().getSupplier().getBundleId() == bundleId)
                    {
                        // Check if this requirement is satisfied by the new
                        // version
                        if (!requirement.isSatisfiedBy(newDescription))
                        {
                            PluginImpl dependentPlugin = getPluginByBundleId(dependent.getBundleId());
                            throw new PluginException("Unable to update bundle: new version does not satisfy dependency from plugin '" + dependentPlugin.getName() + "'");
                        }
                    }
                }
            }
        }
        finally
        {
            // Always remove it: we install purely to check deps.
            uninstallBundle(newPlugin);
        }
    }

    public void uninstallPlugin(Plugin plugin) throws PluginException
    {
        PluginImpl pluginImpl = (PluginImpl) plugin;

        if (pluginImpl.isInternal())
        {
            throw new PluginException("Cannot uninstall an internal plugin");
        }

        switch (pluginImpl.getState())
        {
            case UNINSTALLING:
                throw new PluginException("Cannot uninstall plugin: already marked for uninstall");
            case UPDATING:
                throw new PluginException("Cannot uninstall plugin: already marked for update");
        }

        changeState(pluginImpl, Plugin.State.UNINSTALLING);
    }

    private void changeState(PluginImpl plugin, Plugin.State newState)
    {
        plugin.setState(newState);
        saveState(plugin);
    }

    private void saveState(PluginImpl plugin)
    {
        removeFromPluginsFile(plugin);
        addToPluginsFile(plugin);
    }

    private PluginImpl getPluginByBundle(final Bundle b)
    {
        return getPluginByBundleId(b.getBundleId());
    }

    private PluginImpl getPluginByBundleId(final long id)
    {
        return CollectionUtils.find(plugins, new Predicate<PluginImpl>()
        {
            public boolean satisfied(PluginImpl plugin)
            {
                return id == plugin.getBundle().getBundleId();
            }
        });
    }

    private void uninstallBundle(PluginImpl pluginImpl) throws PluginException
    {
        try
        {
            pluginImpl.getBundle().uninstall();
        }
        catch (BundleException e)
        {
            LOG.severe("Unable to uninstall plugin '" + pluginImpl.getName() + "': " + e.getMessage(), e);
            throw new PluginException("Unable to uninstall plugin: " + e.getMessage() + " (check logs for trace)", e);
        }
    }

    private void removeFromPluginsFile(PluginImpl plugin)
    {
        File pluginDir = getPluginDir(plugin);
        Document doc = readPluginsFile(pluginDir);
        if (doc != null)
        {
            if (removePluginElement(plugin, doc.getRootElement()))
            {
                writePluginsDoc(pluginDir, doc);
            }
        }
    }

    private boolean removePluginElement(PluginImpl plugin, Element rootElement)
    {
        Elements elements = rootElement.getChildElements(ELEMENT_PLUGIN);
        for (int i = 0; i < elements.size(); i++)
        {
            Element element = elements.get(i);
            if (plugin.getId().equals(element.getAttributeValue(ATTRIBUTE_ID)))
            {
                rootElement.removeChild(i);
                return true;
            }
        }

        return false;
    }

    public void enablePlugin(Plugin plugin) throws PluginException
    {
        PluginImpl pluginImpl = (PluginImpl) plugin;
        switch (plugin.getState())
        {
            case DISABLED:
                // Start the plugin if possible
                pluginImpl.setState(Plugin.State.ENABLED);

                try
                {
                    pluginImpl.setBundle(installBundle(pluginImpl.getPluginFile()));
                    startPlugin(pluginImpl);
                }
                catch (BundleException e)
                {
                    LOG.severe("Unable to load plugin '" + pluginImpl.getName() + "': " + e.getMessage(), e);
                    pluginImpl.setState(Plugin.State.DISABLED);
                    pluginImpl.setErrorMessage("Unable to load plugin: " + e.getMessage());
                }

                saveState(pluginImpl);
                break;
            case DISABLING:
                // Unmark for disable
                changeState(pluginImpl, Plugin.State.ENABLED);
                break;
            case ENABLED:
                // Do nothing
                break;
            case UNINSTALLING:
                throw new PluginException("Unable to enable plugin: already marked for uninstall");
            case UPDATING:
                throw new PluginException("Unable to enable plugin: already marked for update");
        }
    }

    public void disablePlugin(Plugin plugin) throws PluginException
    {
        PluginImpl pluginImpl = (PluginImpl) plugin;
        switch (pluginImpl.getState())
        {
            case DISABLED:
            case DISABLING:
                // Nothing to do.
                break;
            case ENABLED:
                changeState(pluginImpl, Plugin.State.DISABLING);
                break;
            case UNINSTALLING:
                throw new PluginException("Unable to disable plugin: already marked for uninstall");
            case UPDATING:
                throw new PluginException("Unable to disable plugin: already marked for update");
        }
    }

    /**
     * Retrieve the plugin that defines the specified extension.
     *
     * @param extension is the extension whose plugin we are retrieving.
     * @return the plugin from which the extension was loaded, or null.
     */
    public Plugin getPlugin(IExtension extension)
    {
        if (!extension.isValid())
        {
            return null;
        }
        Bundle bundle = OSGIUtils.getDefault().getBundle(extension.getNamespaceIdentifier());
        long requiredBundleId = bundle.getBundleId();

        for (PluginImpl plugin : plugins)
        {
            if (plugin.getBundle().getBundleId() == requiredBundleId)
            {
                return plugin;
            }
        }
        return null;
    }

    private File getPluginDir(PluginImpl pluginImpl)
    {
        File pluginDir;
        if (pluginImpl.getType() == PluginImpl.Type.PREPACKAGED)
        {
            pluginDir = pluginPaths.getPrepackagedPluginRoot();
        }
        else
        {
            pluginDir = pluginPaths.getUserPluginRoot();
        }
        return pluginDir;
    }

    public void setPluginPaths(PluginPaths pluginPaths)
    {
        this.pluginPaths = pluginPaths;
    }
}
