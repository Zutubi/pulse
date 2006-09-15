package com.zutubi.validation.i18n;

import java.util.*;
import java.text.MessageFormat;

/**
 * <class-comment/>
 */
public class DefaultTextProvider extends TextProviderSupport
{
    private List defaultResourceBundles = new ArrayList();

    private LocaleProvider localeProvider;

    public DefaultTextProvider(LocaleProvider localeProvider)
    {
        this.localeProvider = localeProvider;
    }

    protected String lookupText(String key, Object... args)
    {
        return findDefaultText(key, localeProvider.getLocale(), args);
    }

    public synchronized void addDefaultResourceBundle(String resourceBundleName)
    {
        // Ensure that this bundle is only in the list once.
        defaultResourceBundles.remove(resourceBundleName);
        defaultResourceBundles.add(0, resourceBundleName);
    }

    public String findDefaultText(String key, Locale locale, Object... args)
    {

        String defaultText = findDefaultText(key, locale);
        if (defaultText != null)
        {
            MessageFormat format = new MessageFormat(defaultText, locale);
            return format.format(args);
        }
        return null;
    }

    /**
     * Returns a localized message for the specified key, aTextName.  Neither the key nor the
     * message is evaluated.
     *
     * @param aTextName the message key
     * @param locale    the locale the message should be for
     * @return a localized message based on the specified key, or null if no localized message can be found for it
     */
    public String findDefaultText(String aTextName, Locale locale)
    {
        List localList;

        synchronized (defaultResourceBundles)
        {
            localList = new ArrayList(defaultResourceBundles);
        }

        for (Iterator iterator = localList.iterator(); iterator.hasNext();)
        {
            String bundleName = (String) iterator.next();

            ResourceBundle bundle = findResourceBundle(bundleName, locale);
            if (bundle != null)
            {
                try
                {
                    return bundle.getString(aTextName);
                }
                catch (MissingResourceException e)
                {
                    // ignore and try others
                }
            }
        }
        return null;
    }

    public ResourceBundle findResourceBundle(String aBundleName, Locale locale)
    {
        try
        {
            return ResourceBundle.getBundle(aBundleName, locale, Thread.currentThread().getContextClassLoader());
        }
        catch (MissingResourceException ex)
        {
            return null;
        }
    }
}
