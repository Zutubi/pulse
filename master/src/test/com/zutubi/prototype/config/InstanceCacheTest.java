package com.zutubi.prototype.config;

import com.zutubi.pulse.test.PulseTestCase;

import java.util.List;
import java.util.LinkedList;

/**
 */
public class InstanceCacheTest extends PulseTestCase
{
    private InstanceCache cache;

    protected void setUp() throws Exception
    {
        cache = new InstanceCache();
    }

    protected void tearDown() throws Exception
    {
        cache = null;
    }

    public void testGetSimpleEmptyCache()
    {
        assertNull(cache.get("trivial"));
    }

    public void testGetEmptyPathEmptyCache()
    {
        assertNull(cache.get(""));
    }

    public void testGetEmptyPath()
    {
        cache.put("anything", new Object());
        assertNull(cache.get(""));
    }

    public void testGetMultipleEmptyCache()
    {
        assertNull(cache.get("multi/part/path"));
    }

    public void testPutEmptyPath()
    {
        Object o = new Object();
        cache.put("", o);
        assertEquals(o, cache.get(""));
    }

    public void testPutSimple()
    {
        Object o = new Object();
        cache.put("simple", o);
        assertEquals(o, cache.get("simple"));
    }

    public void testPutMultipleNoParent()
    {
        Object o = new Object();
        cache.put("multi/part/path", o);
        assertEquals(o, cache.get("multi/part/path"));
    }

    public void testPutMultipleParent()
    {
        Object p = new Object();
        Object c = new Object();
        cache.put("multi", p);
        cache.put("multi/part", c);
        assertEquals(p, cache.get("multi"));
        assertEquals(c, cache.get("multi/part"));
    }

    public void testGetAllEmptyCache()
    {
        List all = new LinkedList();
        cache.getAll("path", all);
        assertEquals(0, all.size());
    }

    public void testGetAllEmptyPath()
    {
        List all = new LinkedList();
        cache.getAll("", all);
        assertEquals(0, all.size());
    }

    public void testGetAllOneMatch()
    {
        Object o = new Object();
        cache.put("foo", new Object());
        cache.put("foo/bar", new Object());
        cache.put("foo/baz", o);
        cache.put("foo/quux", new Object());
        
        List all = new LinkedList();
        cache.getAll("foo/baz", all);
        assertEquals(1, all.size());
        assertEquals(o, all.get(0));
    }

    public void testGetAllMultiMatch()
    {
        Object bar = new Object();
        Object baz = new Object();
        Object quux = new Object();
        cache.put("foo", new Object());
        cache.put("foo/bar", bar);
        cache.put("foo/baz", baz);
        cache.put("foo/quux", quux);

        List all = new LinkedList();
        cache.getAll("foo/*", all);
        assertEquals(3, all.size());
        assertTrue(all.contains(bar));
        assertTrue(all.contains(baz));
        assertTrue(all.contains(quux));
    }
}
