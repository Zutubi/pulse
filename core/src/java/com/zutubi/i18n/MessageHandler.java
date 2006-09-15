package com.zutubi.i18n;

import java.util.Locale;

/**
 * <class-comment/>
 */
public interface MessageHandler
{

    /**
     * Set the locale with the language and country for the
     * current thread.
     *
     * @param locale locale to set
     */
    public void setThreadLocale(Locale locale);

    /**
     * Set globally the locale with the language and country
     *
     * @param locale locale to set
     */
    public void setLocale(Locale locale);

    /**
     * Return currently used locale, either for thread or global.
     *
     * @return currently used locale
     */
    public Locale getLocale();

    public String format(Object context, String key);

    public String format(Object context, String key, Object arg);

    public String format(Object context, String key, Object... args);

    public void clear();
}
