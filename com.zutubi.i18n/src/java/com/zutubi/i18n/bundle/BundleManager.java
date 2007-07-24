package com.zutubi.i18n.bundle;

import com.zutubi.i18n.context.Context;
import com.zutubi.i18n.context.ContextCache;

import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * <class-comment/>
 */
public interface BundleManager
{
    List<ResourceBundle> getBundles(Context context, Locale locale);

    void setContextCache(ContextCache cache);

    void clear();
}
