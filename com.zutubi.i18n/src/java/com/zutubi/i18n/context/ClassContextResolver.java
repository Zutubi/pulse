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

        ReflectionUtils.traverse(context.getContext(), new UnaryProcedure<Class>()
        {
            public void run(Class clazz)
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
