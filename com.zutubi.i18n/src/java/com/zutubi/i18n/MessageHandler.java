package com.zutubi.i18n;

import com.zutubi.i18n.context.Context;

import java.util.Locale;

/**
 * Defines the interface for classes that can format i18n messages using a
 * context.
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

    /**
     * Indicates if the given key can be found and formatted for the given
     * context.
     *
     * @param context the context to format under
     * @param key the key to test for
     * @return true if the given key exists for the given context
     */
    boolean isKeyDefined(Context context, String key);

    /**
     * Retrieve the message for the specified i18n key within the provided
     * context using the default locale.
     *
     * @param context the context to format under
     * @param key     key of the message to format
     * @return the formatted message
     */
    public String format(Context context, String key);

    /**
     * Retrieve the message for the specified i18n key within the provided
     * context using the specifie locale.
     *
     * @param context the context to format under
     * @param key     key of the message to format
     * @param locale  locale to format with
     * @return the formatted message
     */
    public String format(Context context, Locale locale, String key);

    /**
     * Retrieve the message for the specified i18n key within the provided
     * context, formatting it with the given arguemtns, using the default
     * locale.
     *
     * @param context the context to format under
     * @param key     the key of the message to format
     * @param args    arguments used to format the message
     * @return the formatted message
     */
    public String format(Context context, String key, Object... args);

    /**
     * Retrieve the message for the specified i18n key within the provided
     * context, formatting it with the given arguemtns, using the given
     * locale.
     *
     * @param context the context to format under
     * @param locale  the locale to format with
     * @param key     the key of the message to format
     * @param args    arguments used to format the message
     * @return the formatted message
     */
    public String format(Context context, Locale locale, String key, Object... args);

    /**
     * Clear any cached data held by this message handler.
     */
    public void clear();
}
