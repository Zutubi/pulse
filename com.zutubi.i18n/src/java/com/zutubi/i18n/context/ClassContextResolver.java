package com.zutubi.i18n.context;

import java.util.List;
import java.util.LinkedList;

/**
 * <class-comment/>
 */
public class ClassContextResolver implements ContextResolver<ClassContext>
{
    public String[] resolve(ClassContext context)
    {
        Class clazz = context.getContext();

        List<String> resolvedNames = new LinkedList<String>();

        while (clazz != null)
        {
            // step a, the class name
            String className = clazz.getCanonicalName().replace('.', '/');
            resolvedNames.add(className);

            // step b, the interfaces.
            for (Class interfaceClass : clazz.getInterfaces())
            {
                resolvedNames.add(interfaceClass.getCanonicalName().replace('.', '/'));
            }

            clazz = clazz.getSuperclass();
        }

        return resolvedNames.toArray(new String[resolvedNames.size()]);
    }

    public Class<ClassContext> getContextType()
    {
        return ClassContext.class;
    }
}
