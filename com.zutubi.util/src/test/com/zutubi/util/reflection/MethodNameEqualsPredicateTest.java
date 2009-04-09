package com.zutubi.util.reflection;

import com.zutubi.util.junit.ZutubiTestCase;

import java.lang.reflect.Method;

public class MethodNameEqualsPredicateTest extends ZutubiTestCase
{
    private static final String NAME = "theName";

    private Method theName;
    private Method theNameSucks;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();

        theName = MethodHolder.class.getMethod("theName");
        theNameSucks = MethodHolder.class.getMethod("theNameSucks");
    }

    public void testNameMatches()
    {
        MethodNameEqualsPredicate predicate = new MethodNameEqualsPredicate(NAME);
        assertTrue(predicate.satisfied(theName));
    }

    public void testNameDoesntMatch()
    {
        MethodNameEqualsPredicate predicate = new MethodNameEqualsPredicate(NAME);
        assertFalse(predicate.satisfied(theNameSucks));
    }

    private static class MethodHolder
    {
        public void theName()
        {
        }

        public void theNameSucks()
        {
        }
    }
}