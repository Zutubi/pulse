package com.zutubi.util.reflection;

import com.zutubi.util.junit.ZutubiTestCase;

import java.lang.reflect.Method;

public class MethodNamePrefixPredicateTest extends ZutubiTestCase
{
    private static final String PREFIX = "get";

    private Method get;
    private Method getSomething;
    private Method together;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();

        get = MethodHolder.class.getMethod("get");
        getSomething = MethodHolder.class.getMethod("getSomething");
        together = MethodHolder.class.getMethod("together");
    }

    public void testPrefixMatches()
    {
        MethodNamePrefixPredicate predicate = new MethodNamePrefixPredicate(PREFIX, false);
        assertTrue(predicate.apply(getSomething));
    }

    public void testPrefixDoesntMatch()
    {
        MethodNamePrefixPredicate predicate = new MethodNamePrefixPredicate(PREFIX, false);
        assertFalse(predicate.apply(together));
    }

    public void testExactMatchAllowed()
    {
        MethodNamePrefixPredicate predicate = new MethodNamePrefixPredicate(PREFIX, true);
        assertTrue(predicate.apply(get));
        assertTrue(predicate.apply(getSomething));
    }

    public void testExactMatchNotAllowed()
    {
        MethodNamePrefixPredicate predicate = new MethodNamePrefixPredicate(PREFIX, false);
        assertFalse(predicate.apply(get));
        assertTrue(predicate.apply(getSomething));
    }

    private static class MethodHolder
    {
        public void get()
        {

        }

        public void getSomething()
        {

        }

        public void together()
        {

        }
    }
}
