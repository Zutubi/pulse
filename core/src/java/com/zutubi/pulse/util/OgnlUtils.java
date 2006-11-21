package com.zutubi.pulse.util;

import ognl.*;

import java.lang.reflect.Member;
import java.util.Map;

/**
 * <class comment/>
 */
public class OgnlUtils
{
    public static void setProperties(Map<String, Object> details, Object object)
    {
        OgnlContext context = (OgnlContext) Ognl.createDefaultContext(object);
        Ognl.setTypeConverter(context, new TypeConverter()
        {
            public Object convertValue(Map context, Object target, Member member, String propertyName, Object value, Class toType)
            {
                if (toType == Boolean.TYPE && value instanceof String)
                {
                    return Boolean.parseBoolean((String)value);
                }
                return OgnlOps.convertValue(value, toType);
            }
        });

        for (String key : details.keySet())
        {
            try
            {
                Ognl.setValue(key, context, object, details.get(key));
            }
            catch (OgnlException e)
            {
                throw new IllegalArgumentException(String.format("Failed to set '%s' on object '%s'. Cause: %s", key, object, e.getMessage()));
            }
        }
    }
}
