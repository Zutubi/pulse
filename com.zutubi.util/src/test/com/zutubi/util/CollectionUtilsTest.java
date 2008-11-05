package com.zutubi.util;

import com.zutubi.util.junit.ZutubiTestCase;

import java.util.Arrays;
import java.util.List;
import java.util.LinkedList;

public class CollectionUtilsTest extends ZutubiTestCase
{
    public void testReverseEmpty()
    {
        Object[] empty = new Object[0];
        CollectionUtils.reverse(empty);

        // A bit useless, more checking nothing blows up
        assertEquals(0, empty.length);
    }

    public void testReverseOneElement()
    {
        Object a = new Object();
        Object[] oneEl = new Object[]{a};
        CollectionUtils.reverse(oneEl);

        assertEquals(1, oneEl.length);
        assertSame(a, oneEl[0]);
    }

    public void testReverseTwoElements()
    {
        Object a = new Object();
        Object b = new Object();
        Object[] twoEls = new Object[]{a, b};
        CollectionUtils.reverse(twoEls);

        assertEquals(2, twoEls.length);
        assertSame(b, twoEls[0]);
        assertSame(a, twoEls[1]);
    }

    public void testReverseThreeElements()
    {
        Object a = new Object();
        Object b = new Object();
        Object c = new Object();
        Object[] threeEls = new Object[]{a, b, c};
        CollectionUtils.reverse(threeEls);

        assertEquals(3, threeEls.length);
        assertSame(c, threeEls[0]);
        assertSame(b, threeEls[1]);
        assertSame(a, threeEls[2]);
    }

    public void testReverseFourElements()
    {
        Object a = new Object();
        Object b = new Object();
        Object c = new Object();
        Object d = new Object();
        Object[] fourEls = new Object[]{a, b, c, d};
        CollectionUtils.reverse(fourEls);

        assertEquals(4, fourEls.length);
        assertSame(d, fourEls[0]);
        assertSame(c, fourEls[1]);
        assertSame(b, fourEls[2]);
        assertSame(a, fourEls[3]);
    }

    public void testUniqueStrings()
    {
        assertUnique(Arrays.asList("a", "b", "c"), Arrays.asList("a", "b", "b", "c"));
    }

    public void testUniqueEmpty()
    {
        assertUnique(new LinkedList<Object>(), new LinkedList<Object>());
    }

    public void testUniqueContainsNull()
    {
        assertUnique(Arrays.asList("a", null, "c"), Arrays.asList("a", null, null, "c"));
    }

    public void testUniqueOrderMaintained()
    {
        assertUnique(Arrays.asList("c", "1", "a", "5", "2", "x", "n"), Arrays.asList("c", "1", "a", "c", "1", "5", "2", "x", "n", "a"));
    }

    private <T> void assertUnique(List<T> expected, List<T> test)
    {
        List<T> result = CollectionUtils.unique(test);
        assertEquals(expected.size(), result.size());
        for (int i = 0; i < expected.size(); i++)
        {
            assertEquals(expected.get(i), result.get(i));
        }
    }

    public void testTimesZero()
    {
        timesHelper(0);
    }

    public void testTimesOne()
    {
        timesHelper(1);
    }

    public void testTimesTwo()
    {
        timesHelper(2);
    }

    public void testTimesMany()
    {
        timesHelper(20);
    }

    private void timesHelper(int count)
    {
        Object o = new Object();
        List<Object> list = CollectionUtils.times(o, count);
        assertEquals(count, list.size());
        for (Object p: list)
        {
            assertSame(o, p);
        }
    }
}
