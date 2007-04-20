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
}
