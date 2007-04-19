package com.zutubi.i18n.bundle;

import com.zutubi.i18n.context.ContextCache;

import java.util.ResourceBundle;
import java.util.List;
import java.util.Locale;

/**
 * <class-comment/>
 */
public interface BundleManager
{
    List<ResourceBundle> getBundles(Object context, Locale locale);

    void setContextCache(ContextCache cache);

    void clear();
}
