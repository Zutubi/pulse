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

package com.zutubi.pulse.core.scm;

import com.zutubi.pulse.core.scm.api.ScmException;
import com.zutubi.pulse.core.scm.api.WorkingCopy;

import java.lang.reflect.Constructor;
import java.util.Map;
import java.util.TreeMap;

/**
 */
public class WorkingCopyFactory
{
    private static Map<String, Constructor> typeMap = new TreeMap<String, Constructor>();

    public static WorkingCopy create(String type) throws ScmException
    {
        Constructor constructor = typeMap.get(type);
        if(constructor != null)
        {
            try
            {
                return (WorkingCopy) constructor.newInstance();
            }
            catch (Exception e)
            {
                if (e instanceof ScmException)
                {
                    throw (ScmException)e;
                }
                if (e.getCause() instanceof ScmException)
                {
                    throw (ScmException)e.getCause();
                }
                throw new ScmException(e);
            }
        }

        return null;
    }

    public static void registerType(String type, Class clazz) throws IllegalArgumentException, NoSuchMethodException
    {
        if(!WorkingCopy.class.isAssignableFrom(clazz))
        {
            throw new IllegalArgumentException("Class '" + clazz.getName() + "' does not implement WorkingCopy");
        }

        Constructor constructor = clazz.getConstructor();
        typeMap.put(type, constructor);
    }
}
