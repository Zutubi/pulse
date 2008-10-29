package com.zutubi.i18n.bundle;

import com.zutubi.i18n.context.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.Closeable;
import java.util.*;

/**
 * <class-comment/>
 */
public class DefaultBundleManager implements BundleManager
{
    private ResourceBundleFactory factory = new BaseResourceBundleFactory();

    private ContextCache cache;

    private final Map<Class<? extends Context>, List<ContextResolver>> resolvers = new HashMap<Class<? extends Context>, List<ContextResolver>>();

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
        synchronized(resolvers)
        {
            Class clazz = resolver.getContextType();
            if (!resolvers.containsKey(clazz))
            {
                resolvers.put(clazz, new LinkedList<ContextResolver>());
            }
            List<ContextResolver> list = resolvers.get(clazz);
            list.add(resolver);
        }
    }

    public List<ResourceBundle> getBundles(Context context, Locale locale)
    {
        if (cache.isCached(context, locale))
        {
            return cache.getFromCache(context, locale);
        }

        List<ResourceBundle> bundles = new LinkedList<ResourceBundle>();

        List<String> bundleNames = new LinkedList<String>();
        for (ContextResolver resolver : resolvers.get(context.getClass()))
        {
            bundleNames.addAll(Arrays.asList(resolver.resolve(context)));
        }

        bundleNames = filterDuplicates(bundleNames);

        for (String bundleName : bundleNames)
        {
            List<String> candidateNames = factory.expand(bundleName, locale);
            for (String candidateName : candidateNames)
            {
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
                    close(input);
                }
            }
        }

        cache.addToCache(context, locale, bundles);

        return bundles;

    }

    private List<String> filterDuplicates(List<String> bundleNames)
    {
        Set<String> seen = new HashSet<String>();
        List<String> filteredNames = new LinkedList<String>();
        for (String name : bundleNames)
        {
            if (!seen.contains(name))
            {
                seen.add(name);
                filteredNames.add(name);
            }
        }
        return filteredNames;
    }

    private void close(Closeable closeable)
    {
        if (closeable != null)
        {
            try
            {
                closeable.close();
            }
            catch (IOException e)
            {
                // noop.
            }
        }
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
