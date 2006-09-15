package com.zutubi.i18n.context;

import java.util.*;

/**
 * <class-comment/>
 */
public class ExtendedClassContextResolver implements ContextResolver<ClassContext>
{
    public String[] resolve(ClassContext context)
    {
        List<String> resolvedNames = new LinkedList<String>();

        Set<Class> checked = new HashSet<Class>();

        Class clazz = context.getContext();
        while (clazz != null)
        {
            resolvedNames.add(resourceNameFor(clazz));
            checked.add(clazz);

            // for each class, analyse the interfaces.
            for (Class interfaceClazz : clazz.getInterfaces())
            {
                if (!checked.contains(interfaceClazz))
                {
                    resolvedNames.add(resourceNameFor(interfaceClazz));
                    checked.add(interfaceClazz);
                }
            }

            clazz = clazz.getSuperclass();
        }

        // now look at the packages of the base class.
        PackageContextResolver packageResolver = new PackageContextResolver();
        resolvedNames.addAll(Arrays.asList(packageResolver.resolve(new PackageContext(context.getContext()))));

        return resolvedNames.toArray(new String[resolvedNames.size()]);
    }

    private String resourceNameFor(Class clazz)
    {
        return clazz.getName().replace('.', '/');
    }

    public Class<ClassContext> getContextType()
    {
        return ClassContext.class;
    }
}
