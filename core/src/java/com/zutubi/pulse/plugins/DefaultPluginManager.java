package com.zutubi.pulse.plugins;

import com.zutubi.pulse.util.logging.Logger;
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

import java.io.File;
import java.net.URL;
import java.util.Dictionary;
import java.util.LinkedList;
import java.util.List;

public class DefaultPluginManager implements PluginManager
{
    private static final Logger LOG = Logger.getLogger(DefaultPluginManager.class);

    private static final String HEADER_VERSION = "Bundle-Version";
    private static final String HEADER_DESCRIPTION = "Bundle-Description";
    private static final String HEADER_VENDOR = "Bundle-Vendor";

    private PluginPaths pluginPaths;
    private BundleContext context;
    private IExtensionRegistry extensionRegistry;
    private IExtensionTracker extensionTracker;

    public DefaultPluginManager()
    {
    }

    public void init()
    {
        System.setProperty("osgi.configuration.area", pluginPaths.getPluginConfigurationRoot().getAbsolutePath());

        LOG.info("Starting plugin manager...");
        try
        {
            context = EclipseStarter.startup(new String[]{ "-clean" }, null);
            loadInternalPlugins();

            extensionRegistry = RegistryFactory.getRegistry();
            extensionTracker = new ExtensionTracker(extensionRegistry);

            loadPrepackagedPlugins();
            
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
        loadPlugins(pluginPaths.getInternalPluginRoot());
        LOG.info("Internal plugins loaded.");
    }

    private void loadPrepackagedPlugins()
    {
        LOG.info("Loading pre-packaged plugins...");
        loadPlugins(pluginPaths.getPrepackagedPluginRoot());
        LOG.info("Pre-packaged plugins loaded.");
    }

    private void loadPlugins(File pluginDir)
    {
        if (pluginDir.isDirectory())
        {
            List<Bundle> bundles = new LinkedList<Bundle>();

            for (File plugin : pluginDir.listFiles())
            {
                try
                {
                    Bundle bundle = context.installBundle("reference:file:" + plugin.getAbsolutePath());
                    bundles.add(bundle);
                }
                catch (BundleException e)
                {
                    LOG.warning("Unable to load plugin '" + plugin.getAbsolutePath() + "': " + e.getMessage(), e);
                }
            }

            resolveBundles(bundles.toArray(new Bundle[bundles.size()]));

            for (Bundle b : bundles)
            {
                try
                {
                    LOG.info(b.getSymbolicName());
                    b.start(Bundle.START_TRANSIENT);
                }
                catch (BundleException e)
                {
                    LOG.warning("Unable to start plugin '" + b.getSymbolicName() + "': " + e.getMessage(), e);
                }
            }
        }
        else
        {
            LOG.severe("Plugins directory '" + pluginDir.getAbsolutePath() + "' does not exist");
        }
    }

    private void resolveBundles(Bundle[] bundles)
    {
        if (bundles.length == 0)
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

    private Plugin createPlugin(Bundle bundle)
    {
        Plugin p = new Plugin(bundle.getSymbolicName(), Plugin.State.ENABLED);

        Dictionary headers = bundle.getHeaders();
        p.setVersion((String) headers.get(HEADER_VERSION));
        p.setDescription((String) headers.get(HEADER_DESCRIPTION));
        p.setVendor((String) headers.get(HEADER_VENDOR));

        return p;
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
        throw new RuntimeException("Method not yet implemented.");
    }

    public Plugin getPlugin(String name)
    {
        throw new RuntimeException("Method not yet implemented.");
    }

    public Plugin installPlugin(String name, URL url) throws PluginException
    {
        throw new RuntimeException("Method not yet implemented.");
    }

    public Plugin installPlugin(URL url) throws PluginException
    {
        throw new RuntimeException("Method not yet implemented.");
    }

    public void uninstallPlugin(Plugin plugin) throws PluginException
    {
        throw new RuntimeException("Method not yet implemented.");
    }

    public void enablePlugin(Plugin plugin) throws PluginException
    {
        throw new RuntimeException("Method not yet implemented.");
    }

    public void disablePlugin(Plugin plugin) throws PluginException
    {
        throw new RuntimeException("Method not yet implemented.");
    }

    public void setPluginPaths(PluginPaths pluginPaths)
    {
        this.pluginPaths = pluginPaths;
    }

}
