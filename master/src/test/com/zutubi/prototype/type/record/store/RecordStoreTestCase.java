package com.zutubi.prototype.type.record.store;

import com.zutubi.pulse.test.PulseTestCase;
import com.zutubi.prototype.type.record.MutableRecord;
import com.zutubi.prototype.type.record.MutableRecordImpl;
import com.zutubi.prototype.type.record.Record;

import java.util.Random;

/**
 *
 *
 */
public abstract class RecordStoreTestCase extends PulseTestCase
{
    private static final Random RAND = new Random(System.currentTimeMillis());

    protected MutableRecord createRandomSampleRecord()
    {
        MutableRecord randomSample = new MutableRecordImpl();
        for (int i = 0; i < RAND.nextInt(10); i++)
        {
            randomSample.put(Integer.toString(i), Integer.toString(RAND.nextInt(20)));
        }
        return randomSample;
    }

    protected Record createRandomRecord()
    {
        Random rand = new Random(System.currentTimeMillis());
        return createSampleRecord(rand.nextInt(6), rand.nextInt(10));
    }

    protected Record createSampleRecord(int depth, int keys)
    {
        MutableRecordImpl random = new MutableRecordImpl();
        for (int i = 0; i < keys; i++)
        {
            random.put("key" + i, "value");
        }
        for (int i = 0; i < depth; i++)
        {
            random.put("nested" + i, random.copy(true));
        }
        return random;
    }

    public void assertRecordsEquals(Object expected, Object actual)
    {
        assertEquals((Record)expected, (Record)actual);
    }

    public void assertEquals(Record expected, Record actual)
    {
        assertEquals(expected.size(), actual.size());

        assertEquals(expected.keySet(), actual.keySet());
        for (String key : expected.keySet())
        {
            assertEquals(expected.get(key), actual.get(key));
        }

        assertEquals(expected.metaKeySet(), actual.metaKeySet());
        for (String key : expected.metaKeySet())
        {
            assertEquals(expected.getMeta(key), actual.getMeta(key));
        }

        assertEquals(expected.nestedKeySet(), actual.nestedKeySet());
        for (String key : expected.nestedKeySet())
        {
            assertEquals((Record) expected.get(key), (Record) actual.get(key));
        }
    }

}
