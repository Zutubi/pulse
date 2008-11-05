package com.zutubi.util.junit;

import com.zutubi.util.ReflectionUtils;
import junit.framework.TestCase;

import java.lang.reflect.Field;
import java.util.Set;

/**
 * A useful base class for JUnit test cases that nulls out fields in teardown
 * so that memory can be freed.
 */
public class ZutubiTestCase extends TestCase
{
    public ZutubiTestCase()
    {
        super();
    }

    public ZutubiTestCase(String name)
    {
        super(name);
    }

    protected void tearDown() throws Exception
    {
        Set<Field> fields = ReflectionUtils.getDeclaredFields(getClass(), ZutubiTestCase.class);
        for (Field field: fields)
        {
            if (!ReflectionUtils.isFinal(field) && !field.getType().isPrimitive())
            {
                ReflectionUtils.setFieldValue(this, field, null);
            }
        }

        super.tearDown();
    }
}
