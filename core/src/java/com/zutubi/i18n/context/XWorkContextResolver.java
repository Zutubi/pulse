package com.zutubi.i18n.context;

import java.util.List;
import java.util.LinkedList;

/**
 * <class-comment/>
 */
public class XWorkContextResolver implements ContextResolver<XWorkContext>
{
    public String[] resolve(XWorkContext context)
    {
        // Class file.
        List<String> resolvedNames = new LinkedList<String>();

        Class clazz = context.getContext();
        String fullName = clazz.getCanonicalName().replace('.', '/');
        resolvedNames.add(fullName);

        String packageName = clazz.getPackage().getName().replace('.', '/');

        while (packageName.length() > 0)
        {
            resolvedNames.add(packageName + "/package");
            if (packageName.indexOf('/') == -1)
            {
                break;
            }
            packageName = packageName.substring(0, packageName.lastIndexOf('/'));
        }

        resolvedNames.add("package");

        return resolvedNames.toArray(new String[resolvedNames.size()]);
    }

    public Class<XWorkContext> getContextType()
    {
        return XWorkContext.class;
    }
}
