package com.zutubi.util.reflection;

import com.zutubi.util.junit.ZutubiTestCase;

import java.util.List;

public class MethodReturnsTypePredicateTest extends ZutubiTestCase
{
    // Just a sanity check as the logic is tested in ReflectionUtilsTest
    public void testSanity() throws NoSuchMethodException
    {
        MethodReturnsTypePredicate predicate = new MethodReturnsTypePredicate(List.class, String.class);
        assertTrue(predicate.satisfied(MethodHolder.class.getMethod("getStrings")));
        assertFalse(predicate.satisfied(MethodHolder.class.getMethod("getInts")));
        assertFalse(predicate.satisfied(MethodHolder.class.getMethod("getNothing")));
    }
    
    private static class MethodHolder
    {
        public List<String> getStrings()
        {
            return null;
        }

        public List<Integer> getInts()
        {
            return null;
        }

        public void getNothing()
        {
        }
    }
}