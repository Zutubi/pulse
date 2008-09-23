package com.zutubi.tove.type.record;

import java.util.Arrays;

/**
 */
public class RecordUtils
{
    /**
     * Returns true if the two given simple values are equal.  This handles
     * both nulls and the different types of object which may be present as
     * simple values.
     *
     * @param value      the first value
     * @param otherValue the second value
     * @return true if the two values are equal
     */
    public static boolean valuesEqual(Object value, Object otherValue)
    {
        if(value == null)
        {
            return otherValue == null;
        }
        else if(otherValue == null)
        {
            return false;
        }

        if (value.getClass() != otherValue.getClass())
        {
            return false;

        }

        if (value.getClass().isArray())
        {
            return Arrays.equals((Object[])value, (Object[])otherValue);
        }
        else
        {
            return value.equals(otherValue);
        }
    }
}
