package com.zutubi.tove.type.record;

import com.zutubi.util.junit.ZutubiTestCase;

public class MutableRecordImplTest extends ZutubiTestCase
{
    private static final int HANDLE_TOP = 1;
    private static final int HANDLE_NESTED = 11;

    private static final String KEY_TOP_SIMPLE = "simple";
    private static final String VALUE_TOP_SIMPLE = "simpleValue";
    private static final String KEY_NESTED_SIMPLE = "nestedSimple";
    private static final String VALUE_NESTED_SIMPLE = "nestedSimpleValue";
    private static final String KEY_NESTED = "nested";

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
        a.put(KEY_NESTED, nestedA);

        MutableRecordImpl b = new MutableRecordImpl();
        b.put("a", "b");
        MutableRecordImpl nestedB = new MutableRecordImpl();
        nestedB.put("key", "value");
        b.put(KEY_NESTED, nestedB);

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
        a.put(KEY_NESTED, nestedA);

        MutableRecordImpl b = new MutableRecordImpl();
        b.put("a", "b");
        MutableRecordImpl nestedB = new MutableRecordImpl();
        nestedB.put("key", "value");
        b.put(KEY_NESTED, nestedB);

        assertTrue(a.shallowEquals(b));

        nestedB.put("some", "thing");
        assertTrue(a.shallowEquals(b));
    }

    public void testCopy()
    {
        MutableRecordImpl original = new MutableRecordImpl();
        original.setHandle(22);
        original.setSymbolicName("symName");
        original.put(KEY_TOP_SIMPLE, VALUE_TOP_SIMPLE);
        original.putMeta("simpleMeta", "metval");
        
        MutableRecord copy = original.copy(true, true);

        assertEquals(22, copy.getHandle());
        assertEquals("symName", copy.getSymbolicName());
        assertEquals(VALUE_TOP_SIMPLE, copy.get(KEY_TOP_SIMPLE));
        assertEquals("metval", copy.getMeta("simpleMeta"));
    }

    public void testCopyShallow()
    {
        MutableRecordImpl original = createNestedRecords();

        MutableRecord copy = original.copy(false, true);

        assertEquals(HANDLE_TOP, copy.getHandle());
        assertEquals(VALUE_TOP_SIMPLE, copy.get(KEY_TOP_SIMPLE));
        assertSame(original.get(KEY_NESTED), copy.get(KEY_NESTED));
    }

    public void testCopyDeep()
    {
        MutableRecordImpl original = createNestedRecords();

        MutableRecord copy = original.copy(true, true);

        assertEquals(HANDLE_TOP, copy.getHandle());
        assertEquals(VALUE_TOP_SIMPLE, copy.get(KEY_TOP_SIMPLE));
        Object value = copy.get(KEY_NESTED);
        assertNotNull(value);
        assertTrue(value instanceof Record);
        assertNotSame(original.get(KEY_NESTED), value);
        Record nestedCopy = (Record) value;
        assertEquals(HANDLE_NESTED, nestedCopy.getHandle());
        assertEquals(VALUE_NESTED_SIMPLE, nestedCopy.get(KEY_NESTED_SIMPLE));
    }

    public void testCopyDeepNoHandles()
    {
        MutableRecordImpl original = createNestedRecords();

        MutableRecord copy = original.copy(true, false);

        assertEquals(RecordManager.UNDEFINED, copy.getHandle());
        assertEquals(VALUE_TOP_SIMPLE, copy.get(KEY_TOP_SIMPLE));
        Object value = copy.get(KEY_NESTED);
        assertNotNull(value);
        assertTrue(value instanceof Record);
        Record nestedCopy = (Record) value;
        assertEquals(RecordManager.UNDEFINED, nestedCopy.getHandle());
        assertEquals(VALUE_NESTED_SIMPLE, nestedCopy.get(KEY_NESTED_SIMPLE));
    }
    
    private MutableRecordImpl createNestedRecords()
    {
        MutableRecordImpl child = new MutableRecordImpl();
        child.setHandle(HANDLE_NESTED);
        child.put(KEY_NESTED_SIMPLE, VALUE_NESTED_SIMPLE);

        MutableRecordImpl record = new MutableRecordImpl();
        record.setHandle(HANDLE_TOP);
        record.put(KEY_TOP_SIMPLE, VALUE_TOP_SIMPLE);
        record.put(KEY_NESTED, child);
        return record;
    }
}
