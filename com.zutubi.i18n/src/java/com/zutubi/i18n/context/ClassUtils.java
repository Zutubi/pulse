package com.zutubi.i18n.context;

import com.zutubi.util.UnaryProcedure;

import java.util.HashSet;
import java.util.Set;

/**
 * Simple utility used by this package to provide consistent traversal of the
 * class hierarchy between the context resolvers so that they produce consistent
 * results.
 */
final class ClassUtils
{
    static void traverse(Class clazz, UnaryProcedure<Class> c)
    {
        Set<Class> checked = new HashSet<Class>();

        while (clazz != null)
        {
            c.process(clazz);
            checked.add(clazz);

            // for each class, analyse the interfaces.
            for (Class interfaceClazz : clazz.getInterfaces())
            {
                if (!checked.contains(interfaceClazz))
                {
                    c.process(interfaceClazz);
                    checked.add(interfaceClazz);
                }
            }

            clazz = clazz.getSuperclass();
        }
    }
}
