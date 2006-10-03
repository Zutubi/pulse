package com.zutubi.pulse.i18n;

import com.zutubi.i18n.DefaultMessageHandler;
import com.zutubi.i18n.MessageHandler;
import com.zutubi.i18n.bundle.DefaultBundleManager;
import com.zutubi.i18n.context.*;

/**
 * <class-comment/>
 */
public class Messages
{
    private static DefaultMessageHandler handler;

    private static StaticPackageContextResolver packages;

    private static synchronized MessageHandler getHandler()
    {
        if (handler == null)
        {
            packages = new StaticPackageContextResolver();

            DefaultBundleManager bundleManager = new DefaultBundleManager();
            bundleManager.addResolver(new ExtendedClassContextResolver());
            bundleManager.addResolver(packages);
            bundleManager.setContextCache(new DefaultContextCache());
            handler = new DefaultMessageHandler(bundleManager);
        }
        return handler;
    }

    public static String format(Object context, String key)
    {
        if (context instanceof Context)
        {
            return getHandler().format(context, key);
        }
        else if (context instanceof String)
        {
            return getHandler().format(new PackageContext(context), key);
        }
        return getHandler().format(new ClassContext(context), key);
    }

    public static String format(Object context, String key, Object... args)
    {
        if (context instanceof Context)
        {
            return getHandler().format(context, key, args);
        }
        else if (context instanceof String)
        {
            return getHandler().format(new PackageContext(context), key, args);
        }
        return getHandler().format(new ClassContext(context), key, args);
    }

    public void setBundle(String bundleName, String packageName)
    {
        packages.addBundle(new PackageContext(packageName), bundleName);
    }
}
