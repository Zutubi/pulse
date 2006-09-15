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

    private static synchronized MessageHandler getHandler()
    {
        if (handler == null)
        {
            DefaultBundleManager bundleManager = new DefaultBundleManager();
            bundleManager.addResolver(new ExtendedClassContextResolver());
            bundleManager.setContextCache(new DefaultContextCache());
            handler = new DefaultMessageHandler(bundleManager);
        }
        return handler;
    }

    public static String format(Object context, String key)
    {
        if (!(context instanceof Context))
        {
            context = new ClassContext(context);
        }
        return getHandler().format(context, key);
    }

    public static String format(Object context, String key, Object arg)
    {
        if (!(context instanceof Context))
        {
            context = new ClassContext(context);
        }
        return getHandler().format(context, key, arg);
    }

    public static String format(Object context, String key, Object... args)
    {
        if (!(context instanceof Context))
        {
            context = new ClassContext(context);
        }
        return getHandler().format(context, key, args);
    }
}
