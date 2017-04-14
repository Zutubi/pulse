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

package com.zutubi.pulse.core.util;

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
