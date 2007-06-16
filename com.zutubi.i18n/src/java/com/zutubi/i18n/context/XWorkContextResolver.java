package com.zutubi.i18n.context;

import java.util.List;
import java.util.LinkedList;
import java.util.Arrays;

/**
 * <class-comment/>
 */
public class XWorkContextResolver implements ContextResolver<ExtendedClassContext>
{
    public String[] resolve(ExtendedClassContext context)
    {
        // Class file.
        List<String> resolvedNames = new LinkedList<String>();

        Class clazz = context.getContext();
        String fullName = clazz.getCanonicalName().replace('.', '/');
        resolvedNames.add(fullName);

        // now look at the packages of the base class.
        PackageContextResolver packageResolver = new PackageContextResolver();
        resolvedNames.addAll(Arrays.asList(packageResolver.resolve(new PackageContext(context.getContext()))));

        return resolvedNames.toArray(new String[resolvedNames.size()]);
    }

    public Class<ExtendedClassContext> getContextType()
    {
        return ExtendedClassContext.class;
    }
}
