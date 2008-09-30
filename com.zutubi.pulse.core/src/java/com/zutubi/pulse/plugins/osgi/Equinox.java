package com.zutubi.pulse.plugins.osgi;

import com.zutubi.util.logging.Logger;
import org.eclipse.core.runtime.adaptor.EclipseStarter;
import org.eclipse.osgi.framework.internal.core.FrameworkProperties;
import org.eclipse.osgi.framework.util.Headers;
import org.eclipse.osgi.internal.baseadaptor.StateManager;
import org.eclipse.osgi.service.resolver.BundleDescription;
import org.eclipse.osgi.service.resolver.PlatformAdmin;
import org.eclipse.osgi.service.resolver.State;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.packageadmin.PackageAdmin;

import java.io.File;
import java.net.URI;

/**
 *
 *
 */
public class Equinox implements OSGiFramework
{
    private static final Logger LOG = Logger.getLogger(Equinox.class);

    // embedded equinox resources.
    private BundleContext context;
    private ServiceReference packageAdminRef;
    private PackageAdmin packageAdmin;
    private ServiceReference platformAdminRef;
    private PlatformAdmin platformAdmin;

    public void start() throws Exception
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
            platformAdmin = (PlatformAdmin) context.getService(platformAdminRef);
        }

        if (platformAdmin == null)
        {
            throw new RuntimeException("Could not access platform admin service");
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
        return ((StateManager) platformAdmin).getSystemState().getBundles(id).length;
    }

    public BundleDescription getBundleDescription(String symbolicName, String version)
    {
        return ((StateManager) platformAdmin).getSystemState().getBundle(symbolicName, new org.osgi.framework.Version(version));
    }

    public boolean resolveBundles()
    {
        return packageAdmin.resolveBundles(null);
    }

    public boolean resolveBundles(Bundle... bundles)
    {
        return packageAdmin.resolveBundles(bundles);
    }

    public void checkInstallAndResolve(Headers manifest, File source) throws BundleException
    {
        State temporaryState = platformAdmin.getFactory().createState(platformAdmin.getState());
        temporaryState.setResolver(platformAdmin.getResolver());
        temporaryState.setPlatformProperties(FrameworkProperties.getProperties());

        long highestBundleId = 0;
        for (BundleDescription bundle : temporaryState.getBundles())
        {
            if (bundle.getBundleId() > highestBundleId)
            {
                highestBundleId = bundle.getBundleId();
            }
        }

        BundleDescription bundleDescription = platformAdmin.getFactory().createBundleDescription(temporaryState, manifest, getBundleLocation(source), highestBundleId + 1);

        temporaryState.addBundle(bundleDescription);
        temporaryState.resolve();

        if (!bundleDescription.isResolved())
        {
            LOG.info("!bundleDescription.isResolved()");
            // which required are missing?

            // need to evaluate the missing dependencies.
        }
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
}
