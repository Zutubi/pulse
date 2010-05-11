package com.zutubi.tove.squeezer;

import com.zutubi.tove.squeezer.squeezers.*;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Maintains a registry of type converters known as "squeezers".
 */
public class Squeezers
{
    private static final Map<Class, TypeSqueezer> registry = new HashMap<Class, TypeSqueezer>();

    static
    {
        register(String.class, new StringSqueezer());
        register(Boolean.class, new BooleanSqueezer());
        register(Boolean.TYPE, new BooleanSqueezer());
        register(Byte.class, new ByteSqueezer());
        register(Byte.TYPE, new ByteSqueezer());
        register(Character.class, new CharacterSqueezer());
        register(Character.TYPE, new CharacterSqueezer());
        register(Integer.class, new IntegerSqueezer());
        register(Integer.TYPE, new IntegerSqueezer());
        register(Long.class, new LongSqueezer());
        register(Long.TYPE, new LongSqueezer());
        register(Double.class, new DoubleSqueezer());
        register(Double.TYPE, new DoubleSqueezer());
        register(Short.class, new ShortSqueezer());
        register(Short.TYPE, new ShortSqueezer());
        register(Float.class, new FloatSqueezer());
        register(Float.TYPE, new FloatSqueezer());
        register(File.class, new FileSqueezer());
        register(Map.class, new MapSqueezer());
    }

    public static TypeSqueezer findSqueezer(Class<?> type)
    {
        if (type.isEnum())
        {
            return new EnumSqueezer(type.asSubclass(Enum.class));
        }
        else
        {
            return registry.get(type);
        }
    }

    public static void register(Class type, TypeSqueezer squeezer)
    {
        registry.put(type, squeezer);
    }

    public static void unregister(Class type)
    {
        registry.remove(type);
    }
}
