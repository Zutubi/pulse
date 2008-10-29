package com.zutubi.i18n.bundle;

import com.zutubi.i18n.context.Context;
import com.zutubi.i18n.context.ContextCache;

import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * The bundle manager is responsible for managing the association between
 * contexts, locales and resource bundles.
 */
public interface BundleManager
{
    /**
     * Retrieve the list of resource bundles available to the specified
     * context and locale
     *
     * @param context the context that defines the resource bundles
     * @param locale the locale for the resource bundles.
     *
     * @return a list of resource bundles.
     */
    List<ResourceBundle> getBundles(Context context, Locale locale);

    /**
     * Set the context cache implementation to be used by the bundle manager.
     *
     * @param cache context cache instance
     */
    void setContextCache(ContextCache cache);

    /**
     * Clear the context cache.
     */
    void clear();
}
