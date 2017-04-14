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

package com.zutubi.tove.squeezer.squeezers;

import com.zutubi.tove.squeezer.SqueezeException;
import com.zutubi.tove.squeezer.TypeSqueezer;

/**
 * Converts between strings and booleans.  In addition to the standard Java
 * behaviour (a string set to "true", case ignored, is true) this converter
 * also recognises "on" and "yes" (again, case ignored) as true.  Everythig
 * else is false.
 */
public class BooleanSqueezer implements TypeSqueezer
{
    public String squeeze(Object obj) throws SqueezeException
    {
        if (obj == null)
        {
            return "";
        }
        return obj.toString();
    }

    public Boolean unsqueeze(String value) throws SqueezeException
    {
        if (value != null)
        {
            if ("on".equalsIgnoreCase(value))
            {
                return Boolean.TRUE;
            }
            if ("yes".equalsIgnoreCase(value))
            {
                return Boolean.TRUE;
            }
        }
        return Boolean.parseBoolean(value);
    }
}
