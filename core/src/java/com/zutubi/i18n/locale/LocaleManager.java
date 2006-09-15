package com.zutubi.i18n.locale;

import java.util.Locale;

/**
 * <class-comment/>
 */
public class LocaleManager
{
    private static LocaleManager manager = new LocaleManager();
    private static ThreadLocale locale = new ThreadLocale();

    public static LocaleManager getManager()
    {
        return manager;
    }

    /**
     * Set the locale with the language and country for the
     * current thread.
     *
     * @param locale locale to set
     */
    public void setThreadLocale(Locale locale)
    {
        LocaleManager.locale.setThread(true);
        LocaleManager.locale.set(locale);
    }

    /**
     * Set globally the locale with the language and country
     *
     * @param locale locale to set
     */
    public void setLocale(Locale locale)
    {
        LocaleManager.locale.setThread(false);
        LocaleManager.locale.set(locale);
    }

    /**
     * Return currently used locale, either for thread or global.
     *
     * @return currently used locale
     */
    public Locale getLocale()
    {
        return locale.get();
    }


}