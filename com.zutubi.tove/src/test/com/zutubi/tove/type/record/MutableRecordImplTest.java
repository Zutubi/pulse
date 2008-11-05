package com.zutubi.tove.type.record;

import com.zutubi.util.junit.ZutubiTestCase;

/**
 *
 *
 */
public class MutableRecordImplTest extends ZutubiTestCase
{
    public void testEqualsEmpty()
    {
        MutableRecordImpl a = new MutableRecordImpl();
        MutableRecordImpl b = new MutableRecordImpl();

        assertTrue(a.equals(b));
    }

    public void testEqualsSimpleContents()
    {
        MutableRecordImpl a = new MutableRecordImpl();
        a.put("key", "value");
        MutableRecordImpl b = new MutableRecordImpl();
        b.put("key", "value");

        assertTrue(a.equals(b));

        b.put("key", "anotherValue");
        assertFalse(a.equals(b));
    }

    public void testEqualsOtherMissingKey()
    {
        MutableRecordImpl a = new MutableRecordImpl();
        a.put("key", "value");
        MutableRecordImpl b = new MutableRecordImpl();

        assertFalse(a.equals(b));
    }

    public void testEqualsOtherDifferentKey()
    {
        MutableRecordImpl a = new MutableRecordImpl();
        a.put("key", "value");
        MutableRecordImpl b = new MutableRecordImpl();
        b.put("otherkey", "value");

        assertFalse(a.equals(b));
    }

    public void testEqualsArrayContents()
    {
        MutableRecordImpl a = new MutableRecordImpl();
        a.put("key", new String[] {"value"});
        MutableRecordImpl b = new MutableRecordImpl();
        b.put("key", new String[] {"value"});

        assertTrue(a.equals(b));

        b.put("key", new String[] {"value", "value"});
        assertFalse(a.equals(b));
    }

    public void testEqualsNested()
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

    public void testShallowEqualsEmpty()
    {
        MutableRecordImpl a = new MutableRecordImpl();
        MutableRecordImpl b = new MutableRecordImpl();

        assertTrue(a.shallowEquals(b));
    }

    public void testShallowEqualsSimpleContents()
    {
        MutableRecordImpl a = new MutableRecordImpl();
        a.put("key", "value");
        MutableRecordImpl b = new MutableRecordImpl();
        b.put("key", "value");

        assertTrue(a.shallowEquals(b));

        b.put("key", "anotherValue");
        assertFalse(a.shallowEquals(b));
    }

    public void testShallowEqualsOtherMissingKey()
    {
        MutableRecordImpl a = new MutableRecordImpl();
        a.put("key", "value");
        MutableRecordImpl b = new MutableRecordImpl();

        assertFalse(a.shallowEquals(b));
    }

    public void testShallowEqualsOtherDifferentKey()
    {
        MutableRecordImpl a = new MutableRecordImpl();
        a.put("key", "value");
        MutableRecordImpl b = new MutableRecordImpl();
        b.put("otherkey", "value");

        assertFalse(a.shallowEquals(b));
    }

    public void testShallowEqualsArrayContents()
    {
        MutableRecordImpl a = new MutableRecordImpl();
        a.put("key", new String[] {"value"});
        MutableRecordImpl b = new MutableRecordImpl();
        b.put("key", new String[] {"value"});

        assertTrue(a.shallowEquals(b));

        b.put("key", new String[] {"value", "value"});
        assertFalse(a.shallowEquals(b));
    }

    public void testShallowEqualsNested()
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

        assertTrue(a.shallowEquals(b));

        nestedB.put("some", "thing");
        assertTrue(a.shallowEquals(b));
    }
}
