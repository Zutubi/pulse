package com.zutubi.pulse.plugins;

import com.zutubi.pulse.util.FileSystemUtils;
import com.zutubi.pulse.util.IOUtils;
import com.zutubi.pulse.util.logging.Logger;
import nu.xom.*;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.RegistryFactory;
import org.eclipse.core.runtime.adaptor.EclipseStarter;
import org.eclipse.core.runtime.dynamichelpers.ExtensionTracker;
import org.eclipse.core.runtime.dynamichelpers.IExtensionTracker;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.packageadmin.PackageAdmin;

import java.io.*;
import java.net.URL;
import java.util.Collections;
import java.util.Dictionary;
import java.util.LinkedList;
import java.util.List;

public class DefaultPluginManager implements PluginManager
{
    private static final Logger LOG = Logger.getLogger(DefaultPluginManager.class);

    private static final String HEADER_NAME = "Bundle-Name";
    private static final String HEADER_VERSION = "Bundle-Version";
    private static final String HEADER_DESCRIPTION = "Bundle-Description";
    private static final String HEADER_VENDOR = "Bundle-Vendor";

    private static final String ELEMENT_PLUGINS = "plugins";
    private static final String ELEMENT_PLUGIN = "plugin";
    private static final String ATTRIBUTE_ID = "id";
    private static final String ATTRIBUTE_NAME = "name";
    private static final String ATTRIBUTE_FILE = "file";
    private static final String ATTRIBUTE_ENABLED = "enabled";

    private PluginPaths pluginPaths;
    private BundleContext context;
    private IExtensionRegistry extensionRegistry;
    private IExtensionTracker extensionTracker;

    private List<PluginImpl> internalPlugins = new LinkedList<PluginImpl>();
    private List<PluginImpl> plugins = Collections.synchronizedList(new LinkedList<PluginImpl>());

    public DefaultPluginManager()
    {
    }

    public void init()
    {
        System.setProperty("osgi.configuration.area", pluginPaths.getPluginConfigurationRoot().getAbsolutePath());

        LOG.info("Starting plugin manager...");
        try
        {
            context = EclipseStarter.startup(new String[] { "-clean" }, null);
            loadInternalPlugins();

            extensionRegistry = RegistryFactory.getRegistry();
            extensionTracker = new ExtensionTracker(extensionRegistry);

            loadPrepackagedPlugins();

            // Ensure we have a user plugins directory
            File userPlugins = pluginPaths.getUserPluginRoot();
            if (!userPlugins.isDirectory())
            {
                userPlugins.mkdirs();
            }

            loadUserPlugins();

            LOG.info("Plugin manager started.");
        }
        catch (Exception e)
        {
            LOG.severe("Unable to start plugin manager: " + e.getMessage(), e);
        }
    }

    public void destroy()
    {
        try
        {
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
        List<PluginImpl> foundPlugins = loadPlugins(pluginPaths.getInternalPluginRoot(), PluginImpl.Type.INTERNAL);
        internalPlugins.addAll(foundPlugins);
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

    private void startPlugin(PluginImpl plugin)
    {
        Bundle bundle = plugin.getBundle();
        if (bundle.getState() == Bundle.INSTALLED)
        {
            // Resolve the bundle first
            resolveBundles(new Bundle[] { bundle });
        }

        try
        {
            LOG.info("Starting plugin " + bundle.getSymbolicName());
            bundle.start(Bundle.START_TRANSIENT);
        }
        catch (BundleException e)
        {
            plugin.setState(Plugin.State.ERROR);
            plugin.setErrorMessage("Unable to start plugin: " + e.getMessage() + " (see logs for trace)");
            LOG.warning("Unable to start plugin '" + bundle.getSymbolicName() + "': " + e.getMessage(), e);
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
            String id = pluginElement.getAttributeValue(ATTRIBUTE_ID);
            String name = pluginElement.getAttributeValue(ATTRIBUTE_NAME);
            String file = pluginElement.getAttributeValue(ATTRIBUTE_FILE);
            boolean enabled = Boolean.valueOf(pluginElement.getAttributeValue(ATTRIBUTE_ENABLED));

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

            File pluginFile = new File(pluginDir, file);
            if (enabled)
            {
                if (pluginFile.exists())
                {
                    try
                    {
                        foundPlugins.add(loadPluginFile(pluginFile, type));
                    }
                    catch (BundleException e)
                    {
                        LOG.warning("Unable to load plugin '" + name + "': " + e.getMessage(), e);

                        PluginImpl plugin = new PluginImpl(id, name, pluginFile, Plugin.State.ERROR, type);
                        plugin.setErrorMessage("Unable to load plugin '" + name + "': " + e.getMessage() + "(check logs for trace)");
                        foundPlugins.add(plugin);
                    }
                }
                else
                {
                    PluginImpl p = new PluginImpl(id, name, pluginFile, Plugin.State.ERROR, type);
                    p.setErrorMessage("Plugin file '" + pluginFile.getAbsolutePath() + "' does not exist");
                    foundPlugins.add(p);

                    LOG.warning("File '" + pluginFile.getAbsolutePath() + "' not found for listed plugin '" + name + "'");
                }
            }
            else
            {
                foundPlugins.add(new PluginImpl(id, name, pluginFile, Plugin.State.DISABLED, type));
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
                    PluginImpl plugin = loadPluginFile(pluginFile, type);
                    foundPlugins.add(plugin);
                }
                catch (BundleException e)
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

    private PluginImpl loadPluginFile(File pluginFile, PluginImpl.Type type) throws BundleException
    {
        Bundle bundle = installBundle(pluginFile);
        PluginImpl plugin = new PluginImpl(bundle.getSymbolicName(), getBundleName(bundle), pluginFile, Plugin.State.ENABLED, type);
        postInstall(plugin, bundle);

        return plugin;
    }

    private Bundle installBundle(File pluginFile) throws BundleException
    {
        Bundle bundle = context.installBundle("reference:file:" + pluginFile.getAbsolutePath());
        if (bundle.getSymbolicName() == null)
        {
            throw new BundleException("Bundle missing required header Bundle-SymbolicName");
        }

        return bundle;
    }

    private String getBundleName(Bundle bundle)
    {
        Dictionary headers = bundle.getHeaders();
        String name = (String) headers.get(HEADER_NAME);
        if (name == null)
        {
            name = bundle.getSymbolicName();
        }

        return name;
    }

    private void postInstall(PluginImpl plugin, Bundle bundle)
    {
        plugin.setBundle(bundle);
        Dictionary headers = bundle.getHeaders();
        plugin.setVersion((String) headers.get(HEADER_VERSION));
        plugin.setDescription((String) headers.get(HEADER_DESCRIPTION));
        plugin.setVendor((String) headers.get(HEADER_VENDOR));
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
        element.addAttribute(new Attribute(ATTRIBUTE_ENABLED, Boolean.toString(plugin.isEnabled())));
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

        ServiceReference packageAdminRef = context.getServiceReference(PackageAdmin.class.getName());
        PackageAdmin packageAdmin = null;
        if (packageAdminRef != null)
        {
            packageAdmin = (PackageAdmin) context.getService(packageAdminRef);
        }

        if (packageAdmin == null)
        {
            return;
        }

        packageAdmin.resolveBundles(bundles);
        context.ungetService(packageAdminRef);
    }

    public IExtensionRegistry getExtenstionRegistry()
    {
        return extensionRegistry;
    }

    public IExtensionTracker getExtenstionTracker()
    {
        return extensionTracker;
    }

    public List<? extends Plugin> getAllPlugins()
    {
        return plugins;
    }

    public Plugin getPlugin(String id)
    {
        for (PluginImpl plugin : plugins)
        {
            if (plugin.getId().equals(id))
            {
                return plugin;
            }
        }

        return null;
    }

    public Plugin installPlugin(URL url) throws PluginException
    {
        String name = url.getPath();
        int index = name.lastIndexOf('/');
        if (index >= 0)
        {
            name = name.substring(index + 1);
        }

        return installPlugin(name, url);
    }

    public Plugin installPlugin(String name, URL url) throws PluginException
    {
        if (getPlugin(name) != null)
        {
            throw new PluginException("A plugin with name '" + name + "' already exists");
        }

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

        PluginImpl plugin;

        try
        {
            plugin = loadPluginFile(pluginFile, PluginImpl.Type.USER);
        }
        catch (BundleException e)
        {
            LOG.warning("Unable to load plugin from file '" + pluginFile.getAbsolutePath() + "': " + e.getMessage(), e);
            throw new PluginException("Unable to load plugin: " + e.getMessage(), e);
        }

        // Add the plugin to the list (in memory and file)
        plugins.add(plugin);
        addToPluginsFile(pluginPaths.getUserPluginRoot(), plugin);

        // Finally, start it up.
        startPlugin(plugin);
        return plugin;
    }

    private void addToPluginsFile(File pluginDir, PluginImpl plugin)
    {
        Document doc = readPluginsFile(pluginDir);
        if (doc != null)
        {
            addPluginElement(plugin, doc.getRootElement());
            writePluginsDoc(pluginDir, doc);
        }
    }

    public void uninstallPlugin(Plugin plugin) throws PluginException
    {
        // Uninstall the bundle is necessary
        PluginImpl pluginImpl = (PluginImpl) plugin;

        if (pluginImpl.isInternal())
        {
            throw new PluginException("Cannot uninstall an internal plugin");
        }

        if (pluginImpl.isEnabled())
        {
            uninstallBundle(pluginImpl);
        }

        // Remove the actual bundle file
        File pluginFile = pluginImpl.getPluginFile();
        if (pluginFile.isDirectory())
        {
            FileSystemUtils.rmdir(pluginFile);
        }
        else
        {
            pluginFile.delete();
        }

        // Remove from the list of plugins (in memory and file)
        plugins.remove(pluginImpl);
        File pluginDir = getPluginDir(pluginImpl);

        removeFromPluginsFile(pluginDir, pluginImpl);
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

    private void removeFromPluginsFile(File pluginDir, PluginImpl plugin)
    {
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
        if (pluginImpl.isDisabled())
        {
            try
            {
                Bundle bundle = installBundle(pluginImpl.getPluginFile());
                postInstall(pluginImpl, bundle);
            }
            catch (BundleException e)
            {
                LOG.severe("Unable to load plugin '" + pluginImpl.getName() + "': " + e.getMessage(), e);
                throw new PluginException("Unable to load plugin '" + pluginImpl.getName() + "': " + e.getMessage(), e);
            }

            pluginImpl.setState(Plugin.State.ENABLED);

            // Rewrite plugins file
            removeFromPluginsFile(getPluginDir(pluginImpl), pluginImpl);
            addToPluginsFile(getPluginDir(pluginImpl), pluginImpl);

            startPlugin(pluginImpl);
        }
    }

    public void disablePlugin(Plugin plugin) throws PluginException
    {
        PluginImpl pluginImpl = (PluginImpl) plugin;
        if (pluginImpl.isEnabled())
        {
            uninstallBundle(pluginImpl);

            pluginImpl.setState(Plugin.State.DISABLED);

            // Rewrite plugins file
            removeFromPluginsFile(getPluginDir(pluginImpl), pluginImpl);
            addToPluginsFile(getPluginDir(pluginImpl), pluginImpl);
        }
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
