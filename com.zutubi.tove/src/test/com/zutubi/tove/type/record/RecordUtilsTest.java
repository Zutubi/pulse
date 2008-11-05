package com.zutubi.tove.type.record;

import com.zutubi.util.junit.ZutubiTestCase;

public class RecordUtilsTest extends ZutubiTestCase
{
    public void testIsSimpleNull()
    {
        assertFalse(RecordUtils.isSimpleValue(null));
    }
    
    public void testIsSimpleAnotherType()
    {
        assertFalse(RecordUtils.isSimpleValue(new Object()));
    }

    public void testIsSimpleRecord()
    {
        assertFalse(RecordUtils.isSimpleValue(new MutableRecordImpl()));
    }

    public void testIsSimpleString()
    {
        assertTrue(RecordUtils.isSimpleValue("yay"));
    }

    public void testIsSimpleStringArray()
    {
        assertTrue(RecordUtils.isSimpleValue(new String[0]));
    }

    public void testValuesEqualBothNull()
    {
        assertTrue(RecordUtils.valuesEqual(null, null));
    }

    public void testValuesEqualFirstNull()
    {
        assertFalse(RecordUtils.valuesEqual(null, ""));
    }

    public void testValuesEqualSecondNull()
    {
        assertFalse(RecordUtils.valuesEqual("", null));
    }

    public void testValuesEqualEqualStrings()
    {
        assertTrue(RecordUtils.valuesEqual("foo", "foo"));
    }

    public void testValuesEqualUnequalStrings()
    {
        assertFalse(RecordUtils.valuesEqual("foo", "bar"));
    }

    public void testValuesEqualEmptyArrays()
    {
        assertTrue(RecordUtils.valuesEqual(new String[0], new String[0]));
    }

    public void testValuesEqualEqualArrays()
    {
        assertTrue(RecordUtils.valuesEqual(new String[]{"foo", "bar"}, new String[]{"foo", "bar"}));
    }

    public void testValuesEqualUnequalArrays()
    {
        assertFalse(RecordUtils.valuesEqual(new String[]{"bar", "foo"}, new String[]{"foo", "bar"}));
    }

    public void testValuesEqualDifferentArrayClasses()
    {
        assertFalse(RecordUtils.valuesEqual(new String[0], new Object[0]));
    }
}
