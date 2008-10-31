package com.zutubi.i18n.context;

import java.util.Map;
import java.util.HashMap;

/**
 * The IdContextResolver uses ids to lookup pre-configured bundlenames.
 */
public class IdContextResolver implements ContextResolver<IdContext>
{
    private Map<IdContext, String> bundles = new HashMap<IdContext, String>();

    public void addBundle(IdContext context, String bundleName)
    {
        bundles.put(context, bundleName);
    }

    public String[] resolve(IdContext context)
    {
        if (bundles.containsKey(context))
        {
            return new String[]{bundles.get(context)};
        }
        return new String[0];
    }

    public Class<IdContext> getContextType()
    {
        return IdContext.class;
    }
}
