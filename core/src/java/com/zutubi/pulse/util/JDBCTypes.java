package com.zutubi.pulse.util;

import java.sql.Types;
import java.lang.reflect.Field;

/**
 *
 *
 */
public class JDBCTypes
{
    public static String toString(int i)
    {
        try
        {
            for (Field field : Types.class.getFields())
            {
                if (field.getInt(Types.class) == i)
                {
                    return field.getName();
                }
            }
            return "UNKNOWN";
        }
        catch (IllegalAccessException e)
        {
            throw new RuntimeException(e);
        }
    }

    public static int valueOf(String str)
    {
        try
        {
            Field field = Types.class.getField(str);
            return field.getInt(Types.class);
        }
        catch (IllegalAccessException e)
        {
            throw new RuntimeException(e);
        }
        catch (NoSuchFieldException e)
        {
            return Integer.MIN_VALUE;
        }
    }
}
