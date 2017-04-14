/* Copyright 2017 Zutubi Pty Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.zutubi.i18n.context;

import com.zutubi.util.UnaryProcedure;
import com.zutubi.util.reflection.ReflectionUtils;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newLinkedHashSet;

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

        ReflectionUtils.traverse(context.getContext(), new UnaryProcedure<Class>()
        {
            public void run(Class clazz)
            {
                resolvedNames.addAll(Arrays.asList(packageResolver.resolve(new PackageContext(clazz))));
            }
        });

        List<String> filteredNames = newArrayList(newLinkedHashSet(resolvedNames));

        // move the base package to the END of the list.
        filteredNames.remove(PackageContextResolver.BUNDLE_NAME);
        filteredNames.add(PackageContextResolver.BUNDLE_NAME);
        
        return filteredNames.toArray(new String[filteredNames.size()]);
    }

    public Class<ClassContext> getContextType()
    {
        return ClassContext.class;
    }
}
