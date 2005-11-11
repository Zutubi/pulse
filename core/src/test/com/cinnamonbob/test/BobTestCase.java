package com.cinnamonbob.test;

import junit.framework.TestCase;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.io.InputStream;
import java.net.URL;

/**
 * Base class for test cases.
 */
public abstract class BobTestCase extends TestCase
{
    public BobTestCase()
    {

    }

    public BobTestCase(String name)
    {
        super(name);
    }

    protected static void assertObjectEquals(Object a, Object b)
    {
        assertObjectEquals(null, a, b);
    }

    protected static void assertObjectEquals(String msg, Object a, Object b)
    {
        if (a instanceof Map)
        {
            assertEquals(msg, (Map) a, (Map) b);
        }
        else if (a instanceof List)
        {
            assertEquals(msg, (List) a, (List) b);
        }
        else if (a instanceof Collection)
        {
            assertEquals(msg, (Collection) a, (Collection) b);
        }
        else
        {
            assertEquals(msg, a, b);
        }
    }

    protected static void assertEquals(Map a, Map b)
    {
        assertEquals(null, a, b);
    }

    protected static void assertEquals(String msg, Map a, Map b)
    {
        if (msg == null)
            msg = "";
        assertEquals(msg + " [size difference]: ", a.size(), b.size());
        for (Object key : a.keySet())
        {
            assertObjectEquals(msg + " [property '" + key.toString() + "' difference]: ", a.get(key), b.get(key));
        }
    }

    protected static void assertEquals(List a, List b)
    {
        assertEquals(null, a, b);
    }

    protected static void assertEquals(String msg, List a, List b)
    {
        assertEquals(msg, a.size(), b.size());
        for (int i = 0; i < a.size(); i++)
        {
            assertObjectEquals(msg, a.get(i), b.get(i));
        }
    }

    protected static void assertEquals(Collection a, Collection b)
    {
        assertEquals(null, a, b);
    }

    protected static void assertEquals(String msg, Collection a, Collection b)
    {
        assertEquals(msg, a.size(), b.size());
        for (Object aA : a)
        {
            assertTrue(msg, b.contains(aA));
        }
    }

    protected InputStream getInput(String testName)
    {
        return getClass().getResourceAsStream(getClass().getSimpleName() + "." + testName + ".xml");
    }

    protected URL getInputURL(String testName)
    {
        return getClass().getResource(getClass().getSimpleName() + "." + testName + ".xml");
    }
}
