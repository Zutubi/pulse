package com.zutubi.util;

import com.zutubi.util.junit.ZutubiTestCase;

import java.util.LinkedList;
import java.util.Iterator;
import java.util.Arrays;

public class ReverseListIteratorTest extends ZutubiTestCase
{
    public void testEmptyList()
    {
        assertOrdering(list());
    }

    public void testSingleItemInList()
    {
        assertOrdering(list("A"), "A");
    }
    
    public void testMultipleItemsInList()
    {
        assertOrdering(list("A", "B"), "B", "A");
        assertOrdering(list("A", "B", "C"), "C", "B", "A");
    }

    private LinkedList<String> list(String... values)
    {
        return new LinkedList<String>(Arrays.asList(values));
    }

    private void assertOrdering(LinkedList<String> list, String... expectedReverse)
    {
        Iterator<String> i = new ReverseListIterator<String>(list);
        for (String expected : expectedReverse)
        {
            assertTrue(i.hasNext());
            assertEquals(expected,  i.next());
        }
        assertFalse(i.hasNext());
    }
}
