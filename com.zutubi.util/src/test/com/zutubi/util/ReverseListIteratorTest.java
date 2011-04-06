package com.zutubi.util;

import com.zutubi.util.junit.ZutubiTestCase;
import static java.util.Arrays.asList;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class ReverseListIteratorTest extends ZutubiTestCase
{
    public void testEmptyList()
    {
        assertOrdering(Arrays.<String>asList());
    }

    public void testSingleItemInList()
    {
        assertOrdering(asList("A"), "A");
    }
    
    public void testMultipleItemsInList()
    {
        assertOrdering(asList("A", "B"), "B", "A");
        assertOrdering(asList("A", "B", "C"), "C", "B", "A");
    }

    private void assertOrdering(List<String> list, String... expectedReverse)
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
