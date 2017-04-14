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

package com.zutubi.tove;

import com.zutubi.tove.config.api.Configuration;
import com.zutubi.tove.type.Type;
import com.zutubi.util.reflection.ReflectionUtils;

import java.util.HashSet;
import java.util.Set;

/**
 * Helper methods for finding classes associated with configuration types,
 * for example for finding custom wizard classes.
 */
public class ConventionSupport
{
    public static final String I18N_KEY_SUFFIX_LABEL   = ".label";

    public static <T> Class<? extends T> loadClass(Type type, String suffix, Class<T> required)
    {
        // we need to search up the inheritence hierarchy.
        return loadClass(type.getClazz(), suffix, required);
    }

    public static <T> Class<? extends T> loadClass(Class clazz, String suffix, Class<T> required)
    {
        Class searchClass = clazz;
        if (searchClass.isArray() || searchClass.isPrimitive())
        {
            return null;
        }
        
        while (searchClass != null && searchClass != Object.class)
        {
            try
            {
                String className = searchClass.getName() + suffix;
                Class<?> found = searchClass.getClassLoader().loadClass(className);
                if (required.isAssignableFrom(found))
                {
                    return found.asSubclass(required);
                }
            }
            catch (ClassNotFoundException e)
            {
                // noops.
            }
            searchClass = searchClass.getSuperclass();
        }

        // Interfaces are done separately, as there is no longer a total
        // ordering.  Because of this we actually verify that we only find one
        // matching *Actions interface (we don't want silent non-determinism).
        Set<Class> implementedInterfaces = ReflectionUtils.getImplementedInterfaces(clazz, Object.class, true);
        Set<Class> foundInterfaces = new HashSet<>();
        for (Class iface: implementedInterfaces)
        {
            if (iface != Configuration.class && Configuration.class.isAssignableFrom(iface))
            {
                try
                {
                    String interfaceName = iface.getName() + suffix;
                    foundInterfaces.add(iface.getClassLoader().loadClass(interfaceName));
                }
                catch (ClassNotFoundException e)
                {
                    // noops.
                }
            }
        }

        if (foundInterfaces.isEmpty())
        {
            return null;
        }
        else if (foundInterfaces.size() == 1)
        {
            Class<?> found = foundInterfaces.iterator().next();
            if (required.isAssignableFrom(found))
            {
                return found.asSubclass(required);
            }

            return null;
        }
        else
        {
            String message = "Unable to resolve " + suffix + " type for class '" + clazz.getName() + "': multiple candidate interfaces found:";
            for (Class iface: foundInterfaces)
            {
                message += " '" + iface.getName() + "'";
            }
            
            throw new RuntimeException(message);
        }
    }
}
