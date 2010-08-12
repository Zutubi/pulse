package com.zutubi.pulse.core.plugins.osgi;

import com.zutubi.pulse.core.plugins.util.DependencySort;
import com.zutubi.pulse.core.plugins.util.PluginFileFilter;
import com.zutubi.util.CollectionUtils;
import com.zutubi.util.UnaryFunction;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.RegistryFactory;
import org.eclipse.core.runtime.adaptor.EclipseStarter;
import org.eclipse.core.runtime.dynamichelpers.ExtensionTracker;
import org.eclipse.core.runtime.dynamichelpers.IExtensionTracker;
import org.eclipse.osgi.internal.baseadaptor.StateManager;
import org.eclipse.osgi.service.resolver.BundleDescription;
import org.eclipse.osgi.service.resolver.PlatformAdmin;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.packageadmin.PackageAdmin;

import java.io.File;
import java.net.URI;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * Wraps the Equinox framework and low-level plugins (such as the extension
 * registry) in a Pulse-friendly API.
 */
public class Equinox implements OSGiFramework
{
    // embedded equinox resources.
    private BundleContext context;
    private ServiceReference packageAdminRef;
    private PackageAdmin packageAdmin;
    private ServiceReference platformAdminRef;

    private IExtensionRegistry extensionRegistry;
    private IExtensionTracker extensionTracker;
    private StateManager stateManager;

    public void start(File internalPluginDir) throws Exception
    {
        context = EclipseStarter.startup(new String[]{"-clean"}, null);

        packageAdminRef = context.getServiceReference(PackageAdmin.class.getName());
        if (packageAdminRef != null)
        {
            packageAdmin = (PackageAdmin) context.getService(packageAdminRef);
        }

        if (packageAdmin == null)
        {
            throw new RuntimeException("Could not access package admin service");
        }

        platformAdminRef = context.getServiceReference(PlatformAdmin.class.getName());
        if (platformAdminRef != null)
        {
            stateManager = (StateManager) context.getService(platformAdminRef);
        }

        if (stateManager == null)
        {
            throw new RuntimeException("Could not access state manager service");
        }

        startInternalPlugins(internalPluginDir);

        // extension registry is not available until the internal plugins containing the eclipse registry have
        // been loaded, that is, the internal plugins have been started up.
        extensionRegistry = RegistryFactory.getRegistry();
        extensionTracker = new ExtensionTracker(extensionRegistry);
    }

    private void startInternalPlugins(File internalPluginDir) throws BundleException
    {
        List<Bundle> bundles = new LinkedList<Bundle>();
        for (File file : internalPluginDir.listFiles(new PluginFileFilter()))
        {
            bundles.add(installBundle(file));
        }

        resolveBundles();

        bundles = DependencySort.sort(bundles, new DirectDependenciesFunction(bundles));
        for (Bundle bundle: bundles)
        {
            bundle.start(Bundle.START_TRANSIENT);
        }
    }

    public void stop() throws Exception
    {
        if (packageAdminRef != null)
        {
            context.ungetService(packageAdminRef);
        }

        if (platformAdminRef != null)
        {
            context.ungetService(platformAdminRef);
        }

        if (context != null)
        {
            EclipseStarter.shutdown();
        }
    }

    public void setProperty(String key, String value)
    {
        // equinox properties are taken from the System.properties 
        System.setProperty(key, value);
    }

    public Bundle installBundle(File file) throws BundleException
    {
        return context.installBundle(getBundleLocation(file));
    }

    public int getBundleCount(String id)
    {
        return stateManager.getSystemState().getBundles(id).length;
    }

    public BundleDescription getBundleDescription(String symbolicName, String version)
    {
        return stateManager.getSystemState().getBundle(symbolicName, new org.osgi.framework.Version(version));
    }

    public boolean resolveBundles()
    {
        return packageAdmin.resolveBundles(null);
    }

    public boolean resolveBundles(Bundle... bundles)
    {
        return packageAdmin.resolveBundles(bundles);
    }

    private String getBundleLocation(File pluginFile)
    {
        return "reference:file:" + pluginFile.getAbsolutePath();
    }

    public Bundle install(URI plugin) throws BundleException
    {
        File pluginFile = new File(plugin);
        return installBundle(pluginFile);
    }

    public Bundle resolve(URI plugin) throws BundleException
    {
        Bundle bundle = install(plugin);

        if (!resolveBundles(bundle))
        {
            bundle.uninstall();
            //TODO: Would be very useful to know which of the dependencies failed.
            throw new BundleException("Failed to resolve bundle dependencies.");
        }
        return bundle;
    }

    public Bundle activate(URI plugin) throws BundleException
    {
        Bundle bundle = resolve(plugin);

        bundle.start(Bundle.START_TRANSIENT);
        return bundle;
    }

    public IExtensionRegistry getExtensionRegistry()
    {
        return extensionRegistry;
    }

    public IExtensionTracker getExtensionTracker()
    {
        return extensionTracker;
    }

    private class DirectDependenciesFunction implements UnaryFunction<Bundle, Set<Bundle>> 
    {
        private List<Bundle> bundles;

        public DirectDependenciesFunction(List<Bundle> bundles)
        {
            this.bundles = bundles;
        }

        public Set<Bundle> process(Bundle bundle)
        {
            Set<Bundle> result = new HashSet<Bundle>();
            BundleDescription description = stateManager.getSystemState().getBundle(bundle.getBundleId());
    
            BundleDescription[] required = description.getDependents();
            if (required != null)
            {
                for (final BundleDescription r : required)
                {
                    Bundle dependent = CollectionUtils.find(bundles, new BundleSymbolicNamePredicate(r.getSymbolicName()));
                    if (dependent != null)
                    {
                        result.add(dependent);
                    }
                }
            }
            
            return result;
        }
    }
}
