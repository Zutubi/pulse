package com.zutubi.i18n;

import com.zutubi.i18n.locale.LocaleManager;
import com.zutubi.i18n.format.Formatter;
import com.zutubi.i18n.bundle.BundleManager;

import java.util.Locale;
import java.util.ResourceBundle;

/**
 * The default implementation of the MessageHandler interface.
 *
 */
public class DefaultMessageHandler implements MessageHandler
{
    private Formatter formatter = new Formatter();

    protected BundleManager bundleManager;

    public DefaultMessageHandler(BundleManager bundleManager)
    {
        this.bundleManager = bundleManager;
    }

    public void setThreadLocale(Locale locale)
    {
        localeManager().setThreadLocale(locale);
    }

    public void setLocale(Locale locale)
    {
        localeManager().setLocale(locale);
    }

    public Locale getLocale()
    {
        return localeManager().getLocale();
    }

    public String format(Object context, String key)
    {
        return format(context, getLocale(), key);
    }

    public String format(Object context, Locale locale, String key)
    {
        for (ResourceBundle bundle : bundleManager.getBundles(context, locale))
        {
            String formattedText = formatter.format(bundle, key);
            if (formattedText != null)
            {
                return formattedText;
            }
        }
        return null;
    }

    public String format(Object context, String key, Object... args)
    {
        for (ResourceBundle bundle : bundleManager.getBundles(context, getLocale()))
        {
            String formattedText = formatter.format(bundle, key, args);
            if (formattedText != null)
            {
                return formattedText;
            }
        }
        return null;
    }

    public String format(Object context, Locale locale, String key, Object... args)
    {
        for (ResourceBundle bundle : bundleManager.getBundles(context, locale))
        {
            String formattedText = formatter.format(bundle, key, args);
            if (formattedText != null)
            {
                return formattedText;
            }
        }
        return null;
    }

    public void clear()
    {
        bundleManager.clear();
    }

    private LocaleManager localeManager()
    {
        return LocaleManager.getManager();
    }
}
