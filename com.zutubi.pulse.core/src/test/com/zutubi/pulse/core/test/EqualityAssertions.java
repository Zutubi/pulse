package com.zutubi.pulse.core.test;

import junit.framework.Assert;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static junit.framework.Assert.assertTrue;

/**
 * Assertions for higher-level tests of object equality.
 */
public class EqualityAssertions
{
    public static void assertObjectEquals(String msg, Object a, Object b)
    {
        if (a == null)
        {
            Assert.assertNull(b);
            return;
        }

        if (a instanceof Map)
        {
            assertEquals(msg, (Map<?, ?>) a, (Map<?, ?>) b);
        }
        else if (a instanceof List)
        {
            assertEquals(msg, (List) a, (List) b);
        }
        else if (a instanceof Collection)
        {
            assertEquals(msg, (Collection) a, (Collection) b);
        }
        else if (a.getClass().isArray())
        {
            assertTrue(msg, Arrays.equals((Object[]) a, (Object[]) b));
        }
        else
        {
            Assert.assertEquals(msg, a, b);
        }
    }

    public static void assertEquals(Map a, Map b)
    {
        assertEquals(null, a, b);
    }

    public static void assertEquals(String msg, Map a, Map b)
    {
        if (msg == null)
        {
            msg = "";
        }
        Assert.assertEquals(msg + " [size difference]: ", a.size(), b.size());
        for (Object key : a.keySet())
        {
            assertObjectEquals(msg + " [property '" + key.toString() + "' difference]: ", a.get(key), b.get(key));
        }
    }

    public static void assertEquals(List a, List b)
    {
        assertEquals(null, a, b);
    }

    public static void assertEquals(String msg, List a, List b)
    {
        Assert.assertEquals(msg, a.size(), b.size());
        for (int i = 0; i < a.size(); i++)
        {
            assertObjectEquals(msg, a.get(i), b.get(i));
        }
    }

    public static void assertEquals(Collection a, Collection b)
    {
        assertEquals(null, a, b);
    }

    public static void assertEquals(String msg, Collection a, Collection b)
    {
        Assert.assertEquals(msg, a.size(), b.size());
        for (Object aA : a)
        {
            assertTrue(msg, b.contains(aA));
        }
    }

    public static <T> void assertListEquals(List<T> got, T... expected)
    {
        Assert.assertEquals(expected.length, got.size());
        for (int i = 0; i < expected.length; i++)
        {
            Assert.assertEquals(expected[i], got.get(i));
        }
    }
}
