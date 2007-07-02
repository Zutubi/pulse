package com.zutubi.i18n;

import com.zutubi.i18n.context.Context;

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

    boolean isKeyDefined(Context context, String key);

    /**
     * Retrieve the message for the specified i18n key within the provided
     * context.
     *
     * @param context
     * @param key
     *
     * @return
     */
    public String format(Object context, String key);

    public String format(Object context, Locale locale, String key);

    /**
     * Retrieve the message for the specified i18n key within the provided
     * context.
     *
     * @param context
     * @param key
     * @param args
     *
     * @return
     */
    public String format(Object context, String key, Object... args);

    public String format(Object context, Locale locale, String key, Object... args);

    /**
     * Clear any cached data held by this message handler.
     */
    public void clear();
}
