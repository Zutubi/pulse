package com.zutubi.util;

import com.zutubi.util.junit.ZutubiTestCase;

public class DisjunctivePredicateTest extends ZutubiTestCase
{
    public void testAllTrue()
    {
        DisjunctivePredicate<String> p = new DisjunctivePredicate<String>(new TruePredicate<String>(), new TruePredicate<String>());
        assertTrue(p.satisfied(""));
    }

    public void testSomeTrue()
    {
        DisjunctivePredicate<String> p = new DisjunctivePredicate<String>(new TruePredicate<String>(), new FalsePredicate<String>());
        assertTrue(p.satisfied(""));
    }
    
    public void testNoneTrue()
    {
        DisjunctivePredicate<String> p = new DisjunctivePredicate<String>(new FalsePredicate<String>(), new FalsePredicate<String>());
        assertFalse(p.satisfied(""));
    }
}