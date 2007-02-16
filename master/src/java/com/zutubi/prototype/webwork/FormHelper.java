package com.zutubi.prototype.webwork;

import ognl.Ognl;
import ognl.OgnlException;

import java.util.Map;

/**
 *
 *
 */
public class FormHelper
{
    /**
     * Populate the specified object using provided map of data.
     *
     * @param obj
     * @param data
     */
    public static void populateObject(Object obj, Map<String, String> data)
    {
        // handle type conversion errors.. need the validation context here.
        try
        {
            for (String key : data.keySet())
            {
                String value = data.get(key);
                Ognl.setValue(key, obj, value);
            }
        }
        catch (OgnlException e)
        {
            e.printStackTrace();
        }
    }
}
