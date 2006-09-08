package com.zutubi.pulse.form.squeezer;

import com.zutubi.pulse.form.squeezer.squeezers.StringSqueezer;

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
