package com.zutubi.i18n;

import com.zutubi.i18n.bundle.DefaultBundleManager;
import com.zutubi.i18n.context.*;

/**
 * <class-comment/>
 */
public class Messages
{
    private Object context;

    private static DefaultMessageHandler handler;

    private static StaticPackageContextResolver packages;

    private Messages(Object context)
    {
        this.context = context;
    }

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

    /**
     * Add a custom resolver to the I18N bundle manager.
     * 
     * @param resolver
     */
    public static void addResolver(ContextResolver resolver)
    {
        // ensure that the handler is initialised.
        DefaultMessageHandler handler = (DefaultMessageHandler)getHandler();
        DefaultBundleManager bundleManager = (DefaultBundleManager) handler.bundleManager;
        bundleManager.addResolver(resolver);
    }

    public static boolean isKeyDefined(Object context, String key)
    {
        return getHandler().isKeyDefined(getContext(context), key);
    }

    public static String format(Object context, String key)
    {
        return getHandler().format(getContext(context), key);
    }

    public static String format(Object context, String key, Object... args)
    {
        return getHandler().format(getContext(context), key, args);
    }

    public static void setBundle(String bundleName, String packageName)
    {
        packages.addBundle(new PackageContext(packageName), bundleName);
    }

    private static Context getContext(Object obj)
    {
        if (obj instanceof Context)
        {
            return (Context)obj;
        }
        else if (obj instanceof String)
        {
            return new PackageContext(obj);
        }
        else
        {
            return new ExtendedClassContext(obj);
        }
    }

    public static Messages getInstance(Object context)
    {
        return new Messages(getContext(context));
    }

    public boolean isKeyDefined(String key)
    {
        return Messages.isKeyDefined(context, key);
    }

    public String format(String key)
    {
        return Messages.format(context, key);
    }

    public String format(String key, Object... args)
    {
        return Messages.format(context, key, args);
    }
}
