package com.zutubi.i18n.bundle;

import com.zutubi.i18n.context.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * <class-comment/>
 */
public class DefaultBundleManager implements BundleManager
{
    private ResourceBundleFactory factory = new BaseResourceBundleFactory();

    private ContextCache cache;

    private Map<Class<? extends Context>, ContextResolver> resolvers = new HashMap<Class<? extends Context>, ContextResolver>();

    private ContextLoader defaultLoader = new DefaultContextLoader();

    public DefaultBundleManager()
    {
    }

    public DefaultBundleManager(ContextCache cache)
    {
        this.cache = cache;
    }

    public void addResolver(ContextResolver resolver)
    {
        resolvers.put(resolver.getContextType(), resolver);
    }

    public List<ResourceBundle> getBundles(Context context, Locale locale)
    {
        if (cache.isCached(context, locale))
        {
            return cache.getFromCache(context, locale);
        }

        List<ResourceBundle> bundles = new LinkedList<ResourceBundle>();

        String[] bundleNames = resolvers.get(context.getClass()).resolve(context);
        for (String bundleName : bundleNames)
        {
            List<String> candidateNames = factory.expand(bundleName, locale);
            for (String candidateName : candidateNames)
            {
                // TODO: the resource stream lookup will vary based for plugins.
                InputStream input = null;
                try
                {
                    input = getContextLoader(context).loadResource(context, candidateName);
                    if (input != null)
                    {
                        bundles.add(factory.loadBundle(input, locale));
                    }
                }
                catch (IOException e)
                {
                    // noop.
                }
                finally
                {
                    if (input != null)
                    {
                        try
                        {
                            input.close();
                        }
                        catch (IOException e)
                        {
                            // noop.
                        }
                    }
                }
            }
        }

        cache.addToCache(context, locale, bundles);

        return bundles;

    }

    private ContextLoader getContextLoader(Context context)
    {
        return defaultLoader;
    }

    public void setContextCache(ContextCache cache)
    {
        this.cache = cache;
    }

    public void clear()
    {
        this.cache.clear();
    }
}
