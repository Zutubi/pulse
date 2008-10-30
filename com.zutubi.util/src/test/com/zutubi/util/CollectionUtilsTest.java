package com.zutubi.util;

import junit.framework.TestCase;

public class CollectionUtilsTest extends TestCase
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
}
