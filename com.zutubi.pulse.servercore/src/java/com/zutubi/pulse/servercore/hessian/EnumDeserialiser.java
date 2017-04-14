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

package com.zutubi.pulse.servercore.hessian;

import com.caucho.hessian.io.AbstractDeserializer;
import com.caucho.hessian.io.AbstractHessianInput;

import java.io.IOException;

/**
 * Deserialises Java 1.5 enumerations, because hessian barfs on them itself.
 *
 * Adapted from an implementation posted to hessian-interest by Jason Stiefel.
 */
public class EnumDeserialiser extends AbstractDeserializer
{
    public Object readMap(AbstractHessianInput in) throws IOException
    {
        String clazz = null;
        String value = null;

        while (!in.isEnd())
        {
            clazz = in.readString();
            value = in.readString();
        }
        String r = clazz;
        in.readMapEnd();

        if (clazz == null)
        {
            throw new IOException("Expected Enum class name");
        }

        if (value == null)
        {
            throw new IOException("Expected Enum class value");
        }

        /* If the enum type is an inner class, we need to strip the $n. */
        int index = clazz.lastIndexOf('$');
        if(index != -1)
        {
            // Check the remainder of the string is digits
            String remainder = clazz.substring(index + 1);
            if(remainder.matches("[0-9]+"))
            {
                clazz = clazz.substring(0, index);
            }
        }

        clazz = checkRenames(clazz);
        Class enumClazz;
        try
        {
            enumClazz = Thread.currentThread().getContextClassLoader().loadClass(clazz);
        }
        catch (ClassNotFoundException e)
        {
            throw new IOException("Expected Enum class (" + clazz + ") was not found");
        }

        return Enum.valueOf(enumClazz, value);
    }

    private String checkRenames(String clazz)
    {
        if (clazz.equals("com.zutubi.pulse.services.UpgradeState"))
        {
            // Between 2.0.13 and 2.0.14 we repackaged things.
            return "com.zutubi.pulse.servercore.services.UpgradeState";
        }

        return clazz;
    }
}

