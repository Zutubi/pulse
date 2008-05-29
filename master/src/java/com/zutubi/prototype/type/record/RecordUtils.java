package com.zutubi.prototype.type.record;

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

        if(value instanceof Object[])
        {
            if(!(otherValue instanceof Object[]) || !Arrays.equals((Object[])value, (Object[])otherValue))
            {
                return false;
            }
        }
        else if(otherValue instanceof Object[] || !value.equals(otherValue))
        {
            return false;
        }

        return true;
    }

}
