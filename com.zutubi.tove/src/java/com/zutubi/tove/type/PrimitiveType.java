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

package com.zutubi.tove.type;

import com.zutubi.tove.squeezer.SqueezeException;
import com.zutubi.tove.squeezer.Squeezers;
import com.zutubi.tove.squeezer.TypeSqueezer;
import com.zutubi.util.CollectionUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Manages basic numerical, boolean and string values.
 */
public class PrimitiveType extends SimpleType implements Type
{
    private static final Class[] XML_RPC_SUPPORTED_TYPES = { Boolean.class, Double.class, Integer.class, String.class };
    private static final Map<Class, Class> PRIMITIVE_CLASSES_MAP;
    private static final Map<Class, Object> NULL_VALUES_MAP;
    static
    {
        PRIMITIVE_CLASSES_MAP = new HashMap<Class, Class>(8);
        PRIMITIVE_CLASSES_MAP.put(boolean.class, Boolean.class);
        PRIMITIVE_CLASSES_MAP.put(byte.class, Byte.class);
        PRIMITIVE_CLASSES_MAP.put(char.class, Character.class);
        PRIMITIVE_CLASSES_MAP.put(double.class, Double.class);
        PRIMITIVE_CLASSES_MAP.put(int.class, Integer.class);
        PRIMITIVE_CLASSES_MAP.put(float.class, Float.class);
        PRIMITIVE_CLASSES_MAP.put(long.class, Long.class);
        PRIMITIVE_CLASSES_MAP.put(short.class, Short.class);

        NULL_VALUES_MAP = new HashMap<Class, Object>(7);
        NULL_VALUES_MAP.put(byte.class, Byte.MIN_VALUE);
        NULL_VALUES_MAP.put(char.class, Character.MIN_VALUE);
        NULL_VALUES_MAP.put(double.class, Double.MIN_VALUE);
        NULL_VALUES_MAP.put(int.class, Integer.MIN_VALUE);
        NULL_VALUES_MAP.put(float.class, Float.MIN_VALUE);
        NULL_VALUES_MAP.put(long.class, Long.MIN_VALUE);
        NULL_VALUES_MAP.put(short.class, Short.MIN_VALUE);
    }

    private TypeSqueezer squeezer;

    public PrimitiveType(Class type)
    {
        this(type, null);
    }

    public PrimitiveType(Class type, String symbolicName)
    {
        super(type, symbolicName);
        squeezer = Squeezers.findSqueezer(type);
        if (squeezer == null)
        {
            throw new IllegalArgumentException("Unsupported primitive type: " + type);
        }
    }

    public Object instantiate(Object data, Instantiator instantiator) throws TypeException
    {
        try
        {
            if (data instanceof String[])
            {
                return squeezer.unsqueeze(((String[]) data)[0]);
            }
            else if (data instanceof String)
            {
                return squeezer.unsqueeze((String) data);
            }
            return data;
        }
        catch (SqueezeException e)
        {
            throw new TypeConversionException(e.getMessage());
        }
    }

    public String unstantiate(Object instance, String templateOwnerPath) throws TypeException
    {
        if (instance == null)
        {
            return null;
        }
        
        try
        {
            return squeezer.squeeze(instance);
        }
        catch (SqueezeException e)
        {
            throw new TypeException(e);
        }
    }

    public Object toXmlRpc(String templateOwnerPath, Object data) throws TypeException
    {
        if (data == null)
        {
            return null;
        }

        // XML-RPC only supports limited types, in their direct form.
        Class clazz = getClazz();
        if(PRIMITIVE_CLASSES_MAP.containsKey(clazz))
        {
            clazz = PRIMITIVE_CLASSES_MAP.get(clazz);
        }

        String s = (String) data;
        if(CollectionUtils.contains(XML_RPC_SUPPORTED_TYPES, clazz))
        {
            return instantiate(s, null);
        }
        else if(clazz == Byte.class)
        {
            // Convert up to int
            return Byte.valueOf(s).intValue();
        }
        else if(clazz == Float.class)
        {
            // Convert up to double
            return Float.valueOf(s).doubleValue();
        }
        else if(clazz == Short.class)
        {
            // Convert up to int
            return Short.valueOf(s).intValue();
        }
        else
        {
            // Leave as a string.  This includes characters, and unfortunately
            // longs as well (XML-RPC has no direct way to specify a 64 bit
            // int).
            return s;
        }
    }

    public String fromXmlRpc(String templateOwnerPath, Object data, boolean applyDefaults) throws TypeException
    {
        Class clazz = getClazz();
        if(PRIMITIVE_CLASSES_MAP.containsKey(clazz))
        {
            clazz = PRIMITIVE_CLASSES_MAP.get(clazz);
        }

        if(CollectionUtils.contains(XML_RPC_SUPPORTED_TYPES, clazz))
        {
            typeCheck(data, clazz);
            return unstantiate(data, templateOwnerPath);
        }
        else if(clazz == Byte.class)
        {
            typeCheck(data, Integer.class);
            return Byte.toString(((Integer)data).byteValue());
        }
        else if(clazz == Float.class)
        {
            typeCheck(data, Double.class);
            return Float.toString(((Double)data).floatValue());
        }
        else if(clazz == Short.class)
        {
            typeCheck(data, Integer.class);
            return Short.toString(((Integer)data).shortValue());
        }
        else
        {
            typeCheck(data, String.class);
            return (String) data;
        }
    }

    public Object getNullValue()
    {
        return NULL_VALUES_MAP.get(getClazz());
    }
}
