package com.zutubi.i18n.context;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.LinkedList;

/**
 * <class-comment/>
 */
public class StaticPackageContextResolver implements ContextResolver<PackageContext>
{
    private Map<String, String> bundles = new HashMap<String, String>();

    public void addBundle(String packageName, String bundle)
    {
        bundles.put(packageName, bundle);
    }

    public void addBundle(PackageContext context, String bundle)
    {
        addBundle(context.getContext(), bundle);
    }

    public String[] resolve(PackageContext context)
    {
        List<String> resolvedNames = new LinkedList<String>();

        String packageName = context.getContext();

        while (packageName.length() > 0)
        {
            if (bundles.containsKey(packageName))
            {
                resolvedNames.add(bundles.get(packageName));
            }
            if (packageName.indexOf('.') == -1)
            {
                break;
            }
            packageName = packageName.substring(0, packageName.lastIndexOf('.'));
        }

        return resolvedNames.toArray(new String[resolvedNames.size()]);
    }

    public Class<PackageContext> getContextType()
    {
        return PackageContext.class;
    }
}
