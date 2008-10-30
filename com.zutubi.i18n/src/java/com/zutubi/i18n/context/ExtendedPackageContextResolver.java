package com.zutubi.i18n.context;

import com.zutubi.util.UnaryProcedure;
import com.zutubi.util.CollectionUtils;
import com.zutubi.util.Predicate;

import java.util.*;

/**
 * This context resolver navigates the context's class hierarchy and
 * resolves each of the packages.
 */
public class ExtendedPackageContextResolver implements ContextResolver<ClassContext>
{
    public String[] resolve(ClassContext context)
    {
        final List<String> resolvedNames = new LinkedList<String>();
        final PackageContextResolver packageResolver = new PackageContextResolver();

        ClassUtils.traverse(context.getContext(), new UnaryProcedure<Class>()
        {
            public void process(Class clazz)
            {
                resolvedNames.addAll(Arrays.asList(packageResolver.resolve(new PackageContext(clazz))));
            }
        });

        final List<String> filteredNames = new LinkedList<String>();
        CollectionUtils.filter(resolvedNames, new Predicate<String>()
        {
            public boolean satisfied(String s)
            {
                return !filteredNames.contains(s) && s.compareTo(PackageContextResolver.BUNDLE_NAME) != 0;
            }
        }, filteredNames);

        // move the base package to the END of the list.
        filteredNames.add(PackageContextResolver.BUNDLE_NAME);
        
        return filteredNames.toArray(new String[filteredNames.size()]);
    }

    public Class<ClassContext> getContextType()
    {
        return ClassContext.class;
    }
}
