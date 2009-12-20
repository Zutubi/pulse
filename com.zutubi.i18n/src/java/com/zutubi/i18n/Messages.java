package com.zutubi.i18n;

import com.zutubi.i18n.bundle.DefaultBundleManager;
import com.zutubi.i18n.context.*;

/**
 * The Messages object is a pre-configured entry point into the I18N messages
 * package.
 *
 * By default, it supports message lookups using the {@link com.zutubi.i18n.context.PackageContext} and the
 * {@link com.zutubi.i18n.context.ClassContext}.
 *
 * Custom contexts can be supported by adding customer {@link com.zutubi.i18n.context.ContextResolver} via the
 * {@link #addResolver(com.zutubi.i18n.context.ContextResolver)} method.
 *
 */
public class Messages
{
    private static DefaultMessageHandler handler;

    private Object context;

    private Messages(Object context)
    {
        this.context = context;
    }

    private static ContextCache getContextCache()
    {
        if (Boolean.getBoolean("com.zutubi.i18n.disable.cache"))
        {
            return new EmptyContextCache();
        }
        else
        {
            return new DefaultContextCache();
        }
    }

    private static synchronized MessageHandler getHandler()
    {
        if (handler == null)
        {
            DefaultBundleManager bundleManager = new DefaultBundleManager();
            bundleManager.addResolver(new ExtendedClassContextResolver());
            bundleManager.addResolver(new ExtendedPackageContextResolver());
            bundleManager.addResolver(new StaticPackageContextResolver());
            bundleManager.setContextCache(getContextCache());
            handler = new DefaultMessageHandler(bundleManager);
        }
        return handler;
    }

    /**
     * Add a custom resolver to the I18N bundle manager.
     * 
     * @param resolver instance
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

    public static String formatInContext(Object context, String key)
    {
        return getHandler().format(getContext(context), key);
    }

    public static String formatInContext(Object context, String key, Object... args)
    {
        return getHandler().format(getContext(context), key, args);
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
            return new ClassContext(obj);
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
        return Messages.formatInContext(context, key);
    }

    public String format(String key, Object... args)
    {
        return Messages.formatInContext(context, key, args);
    }
}
