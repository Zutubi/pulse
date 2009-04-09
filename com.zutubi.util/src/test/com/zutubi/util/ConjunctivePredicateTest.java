package com.zutubi.util;

import com.zutubi.util.junit.ZutubiTestCase;

public class ConjunctivePredicateTest extends ZutubiTestCase
{
    public void testAllTrue()
    {
        ConjunctivePredicate<String> p = new ConjunctivePredicate<String>(new TruePredicate<String>(), new TruePredicate<String>());
        assertTrue(p.satisfied(""));
    }

    public void testSomeTrue()
    {
        ConjunctivePredicate<String> p = new ConjunctivePredicate<String>(new TruePredicate<String>(), new FalsePredicate<String>());
        assertFalse(p.satisfied(""));
    }
    
    public void testNoneTrue()
    {
        ConjunctivePredicate<String> p = new ConjunctivePredicate<String>(new FalsePredicate<String>(), new FalsePredicate<String>());
        assertFalse(p.satisfied(""));
    }
}
