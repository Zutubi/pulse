package com.zutubi.tove.type.record;

import com.zutubi.util.BinaryProcedure;
import com.zutubi.util.adt.Pair;
import com.zutubi.util.junit.ZutubiTestCase;

import java.util.LinkedList;
import java.util.List;

import static com.zutubi.util.CollectionUtils.asPair;
import static java.util.Arrays.asList;

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
        assertFalse(RecordUtils.valuesEqual(null, "hey"));
    }

    public void testValuesEqualSecondNull()
    {
        assertFalse(RecordUtils.valuesEqual("you", null));
    }

    public void testValuesEqualFirstNullSecondEmptyString()
    {
        assertTrue(RecordUtils.valuesEqual(null, ""));
    }

    public void testValuesEqualFirstEmptyStringSecondNull()
    {
        assertTrue(RecordUtils.valuesEqual("", null));
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
    
    public void testCreateSkeletonOfTrivial()
    {
        assertEquals(new MutableRecordImpl(), RecordUtils.createSkeletonOf(new MutableRecordImpl()));
    }

    public void testCreateSkeletonOfEliminatesSimple()
    {
        MutableRecord record = new MutableRecordImpl();
        record.putMeta("meta", "value");
        record.put("simple", "value");
        assertEquals(new MutableRecordImpl(), RecordUtils.createSkeletonOf(record));
    }

    public void testCreateSkeletonOfPreservesNested()
    {
        MutableRecord record = new MutableRecordImpl();
        record.put("nested", new MutableRecordImpl());
        assertEquals(record, RecordUtils.createSkeletonOf(record));
    }

    public void testCreateSkeletonOfPreservesSymbolicNames()
    {
        MutableRecord record = new MutableRecordImpl();
        record.setSymbolicName("foo");
        MutableRecord nested = new MutableRecordImpl();
        nested.setSymbolicName("bar");
        record.put("nested", nested);
        assertEquals(record, RecordUtils.createSkeletonOf(record));
    }
    
    public void testParallelDepthFirstWalk()
    {
        final String KEY_NAME = "name";
        final List<Pair<String, String>> EXPECTED_NAME_PAIRS = asList(
                asPair("root1", "root2"),
                asPair("both1", "both2"),
                asPair("gc1", "gc2")
        );
        
        
        // Create trees:
        //
        // (root1)         (root2)
        //   both(both1)     both(both2)
        //     gc(gc2)         gc(gc2)
        //   only1(only1)    only2(only2)
        MutableRecord root1 = new MutableRecordImpl();
        root1.put(KEY_NAME, "root1");

        MutableRecord both = new MutableRecordImpl();
        both.put(KEY_NAME, "both1");
        MutableRecord gc = new MutableRecordImpl();
        gc.put(KEY_NAME, "gc1");
        both.put("gc", gc);
        root1.put("both", both);
        
        MutableRecord only1 = new MutableRecordImpl();
        only1.put(KEY_NAME, "only1");
        root1.put("only1", only1);

        
        MutableRecord root2 = new MutableRecordImpl();
        root2.put(KEY_NAME, "root2");
        both = new MutableRecordImpl();
        both.put(KEY_NAME, "both2");
        gc = new MutableRecordImpl();
        gc.put(KEY_NAME, "gc2");
        both.put("gc", gc);
        root2.put("both", both);
        
        MutableRecord only2 = new MutableRecordImpl();
        only2.put(KEY_NAME, "only2");
        root2.put("only2", only2);

        final List<Pair<String, String>> gotNamePairs = new LinkedList<Pair<String, String>>();
        RecordUtils.parallelDepthFirstWalk(root1, root2, new BinaryProcedure<Record, Record>()
        {
            public void run(Record r1, Record r2)
            {
                gotNamePairs.add(asPair((String) r1.get(KEY_NAME), (String) r2.get(KEY_NAME)));
            }
        });
        
        assertEquals(EXPECTED_NAME_PAIRS, gotNamePairs);
    }
}
