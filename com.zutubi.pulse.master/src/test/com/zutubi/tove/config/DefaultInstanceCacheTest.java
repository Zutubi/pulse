package com.zutubi.tove.config;

import com.zutubi.pulse.core.config.AbstractConfiguration;
import com.zutubi.pulse.core.config.Configuration;
import com.zutubi.pulse.test.PulseTestCase;
import com.zutubi.util.Sort;

import java.util.Collections;
import java.util.Comparator;
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

    public void testHasInstancesUnderEmptyEmptyPath()
    {
        assertFalse(cache.hasInstancesUnder(""));
    }

    public void testHasInstancesUnderEmptySinglePath()
    {
        assertFalse(cache.hasInstancesUnder("a"));
    }

    public void testHasInstancesUnderEmptyMultiPath()
    {
        assertFalse(cache.hasInstancesUnder("a/b/c"));
    }

    public void testHasInstancesUnderSingleEmptyPath()
    {
        cache.put("a", new TestConfiguration(1), true);
        assertTrue(cache.hasInstancesUnder(""));
    }

    public void testHasInstancesUnderSingleSinglePath()
    {
        cache.put("a", new TestConfiguration(1), true);
        assertTrue(cache.hasInstancesUnder("a"));
    }

    public void testHasInstancesUnderSingleMultiPath()
    {
        cache.put("a", new TestConfiguration(1), true);
        assertFalse(cache.hasInstancesUnder("a/b/c"));
    }

    public void testHasInstancesUnderMultiEmptyPath()
    {
        cache.put("a/b/c", new TestConfiguration(1), true);
        assertTrue(cache.hasInstancesUnder(""));
    }

    public void testHasInstancesUnderMultiSinglePath()
    {
        cache.put("a/b/c", new TestConfiguration(1), true);
        assertTrue(cache.hasInstancesUnder("a"));
    }

    public void testHasInstancesUnderMultiMultiPath()
    {
        cache.put("a/b/c", new TestConfiguration(1), true);
        assertTrue(cache.hasInstancesUnder("a/b/c"));
    }

    public void testMarkInvalidEmptyPath()
    {
        markInvalidHelper("", true, true, true, true);
    }

    public void testMarkInvalidA()
    {
        markInvalidHelper("a", false, true, true, true);
    }

    public void testMarkInvalidAB()
    {
        markInvalidHelper("a/b", false, false, true, true);
    }
    
    public void testMarkInvalidABC()
    {
        markInvalidHelper("a/b/c", false, false, false, true);
    }

    public void testMarkInvalidAD()
    {
        markInvalidHelper("a/d", false, true, true, false);
    }

    private void markInvalidHelper(String path, boolean a, boolean ab, boolean abc, boolean ad)
    {
        cache.put("a", new TestConfiguration(1), true);
        cache.put("a/b", new TestConfiguration(2), true);
        cache.put("a/b/c", new TestConfiguration(3), true);
        cache.put("a/d", new TestConfiguration(4), true);

        cache.markInvalid(path);
        assertFalse(cache.isValid("", true));
        assertEquals(a, cache.isValid("a", true));
        assertEquals(ab, cache.isValid("a/b", true));
        assertEquals(abc, cache.isValid("a/b/c", true));
        assertEquals(ad, cache.isValid("a/d", true));
    }

    public void testIsValidNoSuchPath()
    {
        assertFalse(cache.isValid("a", true));
    }

    public void testGetSimpleEmptyCache()
    {
        assertNull(cache.get("trivial", true));
    }

    public void testGetEmptyPathEmptyCache()
    {
        assertNull(cache.get("", true));
    }

    public void testGetEmptyPath()
    {
        cache.put("anything", new TestConfiguration(1), true);
        assertNull(cache.get("", true));
    }

    public void testGetMultipleEmptyCache()
    {
        assertNull(cache.get("multi/part/path", true));
    }

    public void testPutEmptyPath()
    {
        TestConfiguration o = new TestConfiguration(1);
        cache.put("", o, true);
        assertEquals(o, cache.get("", true));
    }

    public void testPutSimple()
    {
        TestConfiguration o = new TestConfiguration(1);
        cache.put("simple", o, true);
        assertEquals(o, cache.get("simple", true));
    }

    public void testPutMultipleNoParent()
    {
        TestConfiguration o = new TestConfiguration(1);
        cache.put("multi/part/path", o, true);
        assertEquals(o, cache.get("multi/part/path", true));
    }

    public void testPutMultipleParent()
    {
        TestConfiguration p = new TestConfiguration(1);
        TestConfiguration c = new TestConfiguration(2);
        cache.put("multi", p, true);
        cache.put("multi/part", c, true);
        assertEquals(p, cache.get("multi", true));
        assertEquals(c, cache.get("multi/part", true));
    }

    public void testGetAllEmptyCache()
    {
        List<Configuration> all = new LinkedList<Configuration>();
        cache.getAllMatchingPathPattern("path", all, true);
        assertEquals(0, all.size());
    }

    public void testGetAllEmptyPath()
    {
        List<Configuration> all = new LinkedList<Configuration>();
        cache.getAllMatchingPathPattern("", all, true);
        assertEquals(0, all.size());
    }

    public void testGetAllOneMatch()
    {
        TestConfiguration o = new TestConfiguration(1);
        cache.put("foo", new TestConfiguration(2), true);
        cache.put("foo/bar", new TestConfiguration(3), true);
        cache.put("foo/baz", o, true);
        cache.put("foo/quux", new TestConfiguration(4), true);
        
        List<Configuration> all = new LinkedList<Configuration>();
        cache.getAllMatchingPathPattern("foo/baz", all, true);
        assertEquals(1, all.size());
        assertEquals(o, all.get(0));
    }

    public void testGetAllMultiMatch()
    {
        TestConfiguration bar = new TestConfiguration(1);
        TestConfiguration baz = new TestConfiguration(2);
        TestConfiguration quux = new TestConfiguration(3);
        cache.put("foo", new TestConfiguration(4), true);
        cache.put("foo/bar", bar, true);
        cache.put("foo/baz", baz, true);
        cache.put("foo/quux", quux, true);

        List<Configuration> all = new LinkedList<Configuration>();
        cache.getAllMatchingPathPattern("foo/*", all, true);
        assertEquals(3, all.size());
        assertTrue(all.contains(bar));
        assertTrue(all.contains(baz));
        assertTrue(all.contains(quux));
    }

    public void testForAllEmptyCache()
    {
        forAllHelper();
    }

    public void testForAllSingleEntry()
    {
        TestConfiguration foo = new TestConfiguration(1);
        cache.put("foo", foo, true);
        forAllHelper(new CollectingHandler.Entry(foo, "foo", null));
    }

    public void testForAllNestedEntry()
    {
        TestConfiguration foo = new TestConfiguration(1);
        TestConfiguration fooBar = new TestConfiguration(2);
        cache.put("foo", foo, true);
        cache.put("foo/bar", fooBar, true);
        forAllHelper(new CollectingHandler.Entry(foo, "foo", null), new CollectingHandler.Entry(fooBar, "foo/bar", foo));
    }

    public void testForAllHoleInPath()
    {
        TestConfiguration foo = new TestConfiguration(1);
        TestConfiguration fooBarBaz = new TestConfiguration(2);
        cache.put("foo", foo, true);
        cache.put("foo/bar/baz", fooBarBaz, true);
        forAllHelper(new CollectingHandler.Entry(foo, "foo", null), new CollectingHandler.Entry(fooBarBaz, "foo/bar/baz", null));
    }

    public void testForAllMultipleRoots()
    {
        TestConfiguration foo = new TestConfiguration(1);
        TestConfiguration fooBar = new TestConfiguration(2);
        TestConfiguration baz = new TestConfiguration(3);
        TestConfiguration bazQuux = new TestConfiguration(4);
        cache.put("foo", foo, true);
        cache.put("foo/bar", fooBar, true);
        cache.put("baz", baz, true);
        cache.put("baz/quux", bazQuux, true);
        forAllHelper(new CollectingHandler.Entry(baz, "baz", null), new CollectingHandler.Entry(bazQuux, "baz/quux", baz), new CollectingHandler.Entry(foo, "foo", null), new CollectingHandler.Entry(fooBar, "foo/bar", foo));
    }

    private List<CollectingHandler.Entry> forAllHelper(CollectingHandler.Entry... expected)
    {
        CollectingHandler handler = new CollectingHandler();
        cache.forAllInstances(handler, true);
        assertEquals(expected.length, handler.entries.size());
        final Sort.StringComparator stringComp = new Sort.StringComparator();
        Collections.sort(handler.entries, new Comparator<CollectingHandler.Entry>()
        {
            public int compare(CollectingHandler.Entry o1, CollectingHandler.Entry o2)
            {
                return stringComp.compare(o1.path, o2.path);
            }
        });
        int i = 0;
        for(CollectingHandler.Entry entry: handler.entries)
        {
            assertEquals(expected[i], entry);
            i++;
        }
        return handler.entries;
    }

    static class TestConfiguration extends AbstractConfiguration
    {
        public TestConfiguration(long handle)
        {
            setHandle(handle);
        }
    }

    static class CollectingHandler implements InstanceCache.InstanceHandler
    {
        List<Entry> entries = new LinkedList<Entry>();

        public void handle(Configuration instance, String path, boolean complete, Configuration parentInstance)
        {
            entries.add(new Entry(instance, path, parentInstance));
        }

        static class Entry
        {
            Configuration instance;
            String path;
            Configuration parentInstance;

            public Entry(Configuration instance, String path, Configuration parentInstance)
            {
                this.instance = instance;
                this.path = path;
                this.parentInstance = parentInstance;
            }

            public boolean equals(Object o)
            {
                if (this == o)
                {
                    return true;
                }
                if (o == null || getClass() != o.getClass())
                {
                    return false;
                }

                Entry entry = (Entry) o;
                return instance.equals(entry.instance) && !(parentInstance != null ? !parentInstance.equals(entry.parentInstance) : entry.parentInstance != null) && path.equals(entry.path);
            }

            public int hashCode()
            {
                int result;
                result = instance.hashCode();
                result = 31 * result + path.hashCode();
                result = 31 * result + (parentInstance != null ? parentInstance.hashCode() : 0);
                return result;
            }
        }
    }
}
