package com.zutubi.pulse.form.squeezer;

import com.zutubi.pulse.form.squeezer.squeezers.*;

import java.util.Map;
import java.util.HashMap;

/**
 * <class-comment/>
 */
public class Squeezers
{
    private static final Map<Class, Class<? extends TypeSqueezer>> registry = new HashMap<Class, Class<? extends TypeSqueezer>>();

    static
    {
        // initialise.
        register(String.class, StringSqueezer.class);
        register(Boolean.class, BooleanSqueezer.class);
        register(Boolean.TYPE, BooleanSqueezer.class);
        register(Integer.class, IntegerSqueezer.class);
        register(Integer.TYPE, IntegerSqueezer.class);
        register(Long.class, LongSqueezer.class);
        register(Long.TYPE, LongSqueezer.class);
        register(Double.class, DoubleSqueezer.class);
        register(Double.TYPE, DoubleSqueezer.class);
        register(Short.class, ShortSqueezer.class);
        register(Short.TYPE, ShortSqueezer.class);
        register(Float.class, FloatSqueezer.class);
        register(Float.TYPE, FloatSqueezer.class);
    }

    public static TypeSqueezer findSqueezer(Class type)
    {
        if (registry.containsKey(type))
        {
            try
            {
                return registry.get(type).newInstance();
            }
            catch (Exception e)
            {
                throw new RuntimeException(e);
            }
        }
        return null;
    }

    public static void register(Class type, Class<? extends TypeSqueezer> squeezer)
    {
        registry.put(type, squeezer);
    }

    public static void unregister(Class type)
    {
        registry.remove(type);
    }
}
