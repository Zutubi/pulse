package com.zutubi.i18n.error;

import com.zutubi.i18n.context.*;
import com.zutubi.i18n.MessageHandler;
import com.zutubi.i18n.DefaultMessageHandler;
import com.zutubi.i18n.bundle.DefaultBundleManager;
import com.zutubi.i18n.bundle.BundleManager;

import java.text.MessageFormat;

/**
 * The error handler is an extension on the MessageHandler that provides
 * error code based message lookup.
 *
 */
public class ErrorHandler
{
    private IdContext errorContext;

    private IdContextResolver idResolver;
    private StaticPackageContextResolver packageResolver;

    private MessageHandler handler;

    private String pattern = "{0}: {1}";


    public ErrorHandler()
    {
        // default context.
        this.errorContext = new IdContext("error");

        // supported resolvers.
        this.idResolver = new IdContextResolver();
        this.packageResolver = new StaticPackageContextResolver();

        DefaultBundleManager bundleManager = new DefaultBundleManager(new DefaultContextCache());
        bundleManager.addResolver(idResolver);
        bundleManager.addResolver(packageResolver);

        this.handler = new DefaultMessageHandler(bundleManager);

        // setup the default error bundle.
        setBundle("errors");
    }

    public void setPattern(String pattern)
    {
        this.pattern = pattern;
    }

    public String error(ErrorCode errorCode)
    {
        return MessageFormat.format(pattern, errorCode.getError(), handler.format(errorContext, errorCode.getError()));
    }

    public String error(ErrorCode errorCode, Object arg)
    {
        return MessageFormat.format(pattern, errorCode.getError(), handler.format(errorContext, errorCode.getError(), arg));
    }

    public String error(ErrorCode errorCode, Object... args)
    {
        return MessageFormat.format(pattern, errorCode.getError(), handler.format(errorContext, errorCode.getError(), args));
    }

    public void setBundle(String bundleName)
    {
        idResolver.addBundle(errorContext, bundleName);
    }

    public void setBundle(String bundleName, String packageName)
    {
        packageResolver.addBundle(new PackageContext(packageName), bundleName);
    }
}