package com.zutubi.i18n.context;

import com.zutubi.util.UnaryProcedure;

import java.util.LinkedList;
import java.util.List;

/**
 * <class-comment/>
 */
public class ClassContextResolver implements ContextResolver<ClassContext>
{
    public String[] resolve(ClassContext context)
    {
        final List<String> resolvedNames = new LinkedList<String>();

        ClassUtils.traverse(context.getContext(), new UnaryProcedure<Class>()
        {
            public void process(Class clazz)
            {
                // step a, the class name
                String className = clazz.getCanonicalName().replace('.', '/');
                resolvedNames.add(className);
            }
        });

        return resolvedNames.toArray(new String[resolvedNames.size()]);
    }

    public Class<ClassContext> getContextType()
    {
        return ClassContext.class;
    }
}
