package com.zutubi.prototype.type.record;

import junit.framework.TestCase;

/**
 *
 *
 */
public class MutableRecordImplTest extends TestCase
{
    public void testEmptyEquals()
    {
        MutableRecordImpl a = new MutableRecordImpl();
        MutableRecordImpl b = new MutableRecordImpl();

        assertTrue(a.equals(b));
    }

    public void testSimpleContentsEquals()
    {
        MutableRecordImpl a = new MutableRecordImpl();
        a.put("key", "value");
        MutableRecordImpl b = new MutableRecordImpl();
        b.put("key", "value");

        assertTrue(a.equals(b));

        b.put("key", "anotherValue");
        assertFalse(a.equals(b));
    }

    public void testNestedEquals()
    {
        MutableRecordImpl a = new MutableRecordImpl();
        a.put("a", "b");
        MutableRecordImpl nestedA = new MutableRecordImpl();
        nestedA.put("key", "value");
        a.put("nested", nestedA);

        MutableRecordImpl b = new MutableRecordImpl();
        b.put("a", "b");
        MutableRecordImpl nestedB = new MutableRecordImpl();
        nestedB.put("key", "value");
        b.put("nested", nestedB);

        assertTrue(a.equals(b));

        nestedB.put("some", "thing");
        assertFalse(a.equals(b));
    }

    public void testRecordMerge()
    {
        MutableRecordImpl a = new MutableRecordImpl();
        a.put("a", "a");
        MutableRecordImpl b = new MutableRecordImpl();
        b.put("b", "b");

        a.merge(b);

        assertEquals("b", a.get("b"));
        assertEquals("a", a.get("a"));
    }

    public void testRecordMergeWithOverwrite()
    {
        MutableRecordImpl a = new MutableRecordImpl();
        a.put("a", "a");
        MutableRecordImpl b = new MutableRecordImpl();
        b.put("a", "b");

        a.merge(b);

        assertEquals("b", a.get("a"));
    }

    public void testNestedRecordMerge()
    {
        MutableRecordImpl a = new MutableRecordImpl();
        a.put("a", "a");
        MutableRecordImpl b = new MutableRecordImpl();
        MutableRecordImpl b1 = new MutableRecordImpl();
        b1.put("b", "b1");
        b.put("b", b1);

        a.merge(b);

        assertEquals("a", a.get("a"));
        assertNotNull(a.get("b"));
        assertEquals("b1", ((Record)a.get("b")).get("b"));
    }

    public void testNestedRecordMergeWithOverwrite()
    {
        MutableRecordImpl a = new MutableRecordImpl();
        MutableRecordImpl a1 = new MutableRecordImpl();
        a1.put("a", "a1");
        a.put("a", a1);
        MutableRecordImpl b = new MutableRecordImpl();
        MutableRecordImpl b1 = new MutableRecordImpl();
        b1.put("a", "b1");
        b.put("a", b1);

        assertEquals("a1", ((Record)a.get("a")).get("a"));

        a.merge(b);

        assertEquals("b1", ((Record)a.get("a")).get("a"));
    }

    public void testRecordDiff()
    {
        MutableRecordImpl a = new MutableRecordImpl();
        a.put("a", "a");
        MutableRecordImpl b = new MutableRecordImpl();
        b.put("b", "b");

        MutableRecordImpl c = a.diff(b);
        assertEquals(1, c.size());
        assertFalse(c.containsKey("a"));
        assertEquals("b", c.get("b"));
    }

    public void testRecordDiffWithOverlap()
    {
        MutableRecordImpl a = new MutableRecordImpl();
        a.put("a", "a");
        MutableRecordImpl b = new MutableRecordImpl();
        b.put("a", "a");
        b.put("b", "b");

        MutableRecordImpl c = a.diff(b);
        assertEquals(1, c.size());
        assertFalse(c.containsKey("a"));
        assertEquals("b", c.get("b"));
    }

    public void testNestedRecordDiff()
    {
        MutableRecordImpl a = new MutableRecordImpl();
        a.put("a", "a");
        MutableRecordImpl b = new MutableRecordImpl();
        MutableRecordImpl b1 = new MutableRecordImpl();
        b1.put("b", "b");
        b.put("b", b1);

        MutableRecordImpl c = a.diff(b);
        assertEquals(1, c.size());
        assertEquals("b", ((Record)c.get("b")).get("b"));

    }
}
