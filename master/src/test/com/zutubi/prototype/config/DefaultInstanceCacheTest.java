package com.zutubi.prototype.config;

import com.zutubi.pulse.core.config.AbstractConfiguration;
import com.zutubi.pulse.core.config.Configuration;
import com.zutubi.pulse.test.PulseTestCase;

import java.util.LinkedList;
import java.util.List;

/**
 */
public class DefaultInstanceCacheTest extends PulseTestCase
{
    private DefaultInstanceCache cache;

    protected void setUp() throws Exception
    {
        cache = new DefaultInstanceCache();
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
        cache.put("anything", new TestConfiguration());
        assertNull(cache.get(""));
    }

    public void testGetMultipleEmptyCache()
    {
        assertNull(cache.get("multi/part/path"));
    }

    public void testPutEmptyPath()
    {
        TestConfiguration o = new TestConfiguration();
        cache.put("", o);
        assertEquals(o, cache.get(""));
    }

    public void testPutSimple()
    {
        TestConfiguration o = new TestConfiguration();
        cache.put("simple", o);
        assertEquals(o, cache.get("simple"));
    }

    public void testPutMultipleNoParent()
    {
        TestConfiguration o = new TestConfiguration();
        cache.put("multi/part/path", o);
        assertEquals(o, cache.get("multi/part/path"));
    }

    public void testPutMultipleParent()
    {
        TestConfiguration p = new TestConfiguration();
        TestConfiguration c = new TestConfiguration();
        cache.put("multi", p);
        cache.put("multi/part", c);
        assertEquals(p, cache.get("multi"));
        assertEquals(c, cache.get("multi/part"));
    }

    public void testGetAllEmptyCache()
    {
        List<Configuration> all = new LinkedList<Configuration>();
        cache.getAllMatchingPathPattern("path", all);
        assertEquals(0, all.size());
    }

    public void testGetAllEmptyPath()
    {
        List<Configuration> all = new LinkedList<Configuration>();
        cache.getAllMatchingPathPattern("", all);
        assertEquals(0, all.size());
    }

    public void testGetAllOneMatch()
    {
        TestConfiguration o = new TestConfiguration();
        cache.put("foo", new TestConfiguration());
        cache.put("foo/bar", new TestConfiguration());
        cache.put("foo/baz", o);
        cache.put("foo/quux", new TestConfiguration());
        
        List<Configuration> all = new LinkedList<Configuration>();
        cache.getAllMatchingPathPattern("foo/baz", all);
        assertEquals(1, all.size());
        assertEquals(o, all.get(0));
    }

    public void testGetAllMultiMatch()
    {
        TestConfiguration bar = new TestConfiguration();
        TestConfiguration baz = new TestConfiguration();
        TestConfiguration quux = new TestConfiguration();
        cache.put("foo", new TestConfiguration());
        cache.put("foo/bar", bar);
        cache.put("foo/baz", baz);
        cache.put("foo/quux", quux);

        List<Configuration> all = new LinkedList<Configuration>();
        cache.getAllMatchingPathPattern("foo/*", all);
        assertEquals(3, all.size());
        assertTrue(all.contains(bar));
        assertTrue(all.contains(baz));
        assertTrue(all.contains(quux));
    }

    public void testForAllEmptyCache()
    {
        cache.forAllInstances(new InstanceCache.InstanceHandler()
        {
            public void handle(Configuration instance, String path, Configuration parentInstance)
            {
                fail();
            }
        });
    }

    private static class TestConfiguration extends AbstractConfiguration
    {
        // Empty
    }
}
