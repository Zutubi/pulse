package com.zutubi.i18n.context;

import com.zutubi.util.UnaryProcedure;

import java.util.*;

/**
 * Simplar to the ClassContext resolver, this resolver implementation applies the PackageContext
 * resolver at the end of the resolution process.
 */
public class ExtendedClassContextResolver implements ContextResolver<ClassContext>
{
    public String[] resolve(ClassContext context)
    {
        final List<String> resolvedNames = new LinkedList<String>();

        ClassUtils.traverse(context.getContext(), new UnaryProcedure<Class>()
        {
            public void process(Class clazz)
            {
                resolvedNames.add(resourceNameFor(clazz));
            }
        });

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
