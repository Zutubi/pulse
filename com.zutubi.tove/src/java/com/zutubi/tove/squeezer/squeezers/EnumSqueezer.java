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
import com.zutubi.util.EnumUtils;
import com.zutubi.util.StringUtils;

/**
 * A type squeezer that converts to and from enums.  Assumes a naming
 * convention of all upper case words separated by underscores to convert
 * symbolic names to and from human-readable strings.
 */
public class EnumSqueezer implements TypeSqueezer
{
    private Class<? extends Enum> enumClass;

    public EnumSqueezer(Class<? extends Enum> enumClass)
    {
        this.enumClass = enumClass;
    }

    public String squeeze(Object obj) throws SqueezeException
    {
        if (obj == null)
        {
            return "";
        }
        return EnumUtils.toPrettyString(((Enum)obj));
    }

    public Object unsqueeze(String s) throws SqueezeException
    {
        if (!StringUtils.stringSet(s))
        {
            return null;
        }

        try
        {
            return EnumUtils.fromPrettyString(enumClass, s);
        }
        catch (IllegalArgumentException e)
        {
            throw new SqueezeException("Invalid value '" + s + "'");
        }
    }
}
