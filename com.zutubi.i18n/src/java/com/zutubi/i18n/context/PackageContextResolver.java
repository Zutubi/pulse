package com.zutubi.i18n.context;

import java.util.List;
import java.util.LinkedList;

/**
 * <class-comment/>
 */
public class PackageContextResolver implements ContextResolver<PackageContext>
{
    protected static final String BUNDLE_NAME = "package";

    public String[] resolve(PackageContext context)
    {
        String packageName = context.getContext().replace('.', '/');

        List<String> resolvedNames = new LinkedList<String>();

        while (packageName.length() > 0)
        {
            resolvedNames.add(packageName + "/" + BUNDLE_NAME);
            if (packageName.indexOf('/') == -1)
            {
                break;
            }
            packageName = packageName.substring(0, packageName.lastIndexOf('/'));
        }

        resolvedNames.add(BUNDLE_NAME);

        return resolvedNames.toArray(new String[resolvedNames.size()]);
    }

    public Class<PackageContext> getContextType()
    {
        return PackageContext.class;
    }
}
