package com.zutubi.util.reflection;

import com.zutubi.util.junit.ZutubiTestCase;

public class MethodAcceptsParametersPredicateTest extends ZutubiTestCase
{
    // Just a sanity check as the logic is tested in ReflectionUtilsTest
    public void testSanity() throws NoSuchMethodException
    {
        MethodAcceptsParametersPredicate predicate = new MethodAcceptsParametersPredicate(Object.class);
        assertTrue(predicate.satisfied(Object.class.getMethod("equals", Object.class)));
        assertFalse(predicate.satisfied(Object.class.getMethod("hashCode")));
    }
}
