package com.zutubi.pulse.core.plugins.osgi;

import com.google.common.base.Function;
import com.zutubi.pulse.core.plugins.util.DependencySort;
import com.zutubi.pulse.core.plugins.util.PluginFileFilter;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.RegistryFactory;
import org.eclipse.core.runtime.adaptor.EclipseStarter;
import org.eclipse.core.runtime.dynamichelpers.ExtensionTracker;
import org.eclipse.core.runtime.dynamichelpers.IExtensionTracker;
import org.eclipse.core.runtime.jobs.IJobManager;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.wiring.BundleWire;
import org.osgi.framework.wiring.BundleWiring;
import org.osgi.framework.wiring.FrameworkWiring;

import java.io.File;
import java.net.URI;
import java.util.*;

import static com.google.common.collect.Iterables.find;

/**
 * Wraps the Equinox framework and low-level plugins (such as the extension
 * registry) in a Pulse-friendly API.
 */
public class Equinox implements OSGiFramework
{
    // embedded equinox resources.
    private BundleContext context;
    private FrameworkWiring frameworkWiring;
    private ServiceReference jobManagerRef;
    private IJobManager jobManager;

    private IExtensionRegistry extensionRegistry;
    private IExtensionTracker extensionTracker;

    public void start(File internalPluginDir) throws Exception
    {
        context = EclipseStarter.startup(new String[0], null);

        frameworkWiring = context.getBundle().adapt(FrameworkWiring.class);
        if (frameworkWiring == null)
        {
            throw new RuntimeException("Could not access framework wiring service");
        }

        startInternalPlugins(internalPluginDir);

        // extension registry is not available until the internal plugins containing the eclipse registry have
        // been loaded, that is, the internal plugins have been started up.
        extensionRegistry = RegistryFactory.getRegistry();
        extensionTracker = new ExtensionTracker(extensionRegistry);

        jobManagerRef = context.getServiceReference(IJobManager.class.getName());
        if (jobManagerRef != null)
        {
            jobManager = (IJobManager) context.getService(jobManagerRef);
        }
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
        if (jobManagerRef != null)
        {
            context.ungetService(jobManagerRef);
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
        int count = 0;
        for (Bundle bundle : context.getBundles())
        {
            if (bundle.getSymbolicName().equals(id))
            {
                count++;
            }
        }
        return count;
    }

    public boolean resolveBundles()
    {
        return frameworkWiring.resolveBundles(Collections.<Bundle>emptyList());
    }

    public boolean resolveBundles(Bundle... bundles)
    {
        return frameworkWiring.resolveBundles(Arrays.asList(bundles));
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

    public IJobManager getJobManager()
    {
        return jobManager;
    }

    private class DirectDependenciesFunction implements Function<Bundle, Set<Bundle>> 
    {
        private List<Bundle> bundles;

        public DirectDependenciesFunction(List<Bundle> bundles)
        {
            this.bundles = bundles;
        }

        public Set<Bundle> apply(Bundle bundle)
        {
            Set<Bundle> result = new HashSet<Bundle>();
            BundleWiring wiring = bundle.adapt(BundleWiring.class);
            if (wiring != null)
            {
                List<BundleWire> providedWires = wiring.getProvidedWires(null);
                if (providedWires != null)
                {
                    for (final BundleWire p : providedWires)
                    {
                        Bundle dependent = find(bundles, new BundleSymbolicNamePredicate(p.getRequirer().getSymbolicName()), null);
                        if (dependent != null)
                        {
                            result.add(dependent);
                        }
                    }
                }
            }
            
            return result;
        }
    }
}
