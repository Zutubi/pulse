package com.zutubi.prototype.type.record;

import com.zutubi.pulse.test.PulseTestCase;
import com.zutubi.pulse.util.FileSystemUtils;

import java.io.File;
import java.io.IOException;
import java.util.Random;

/**
 *
 *
 */
public class XmlRecordSerialiserTest extends PulseTestCase
{
    private XmlRecordSerialiser serialiser;

    private File tmpDir;

    private static final Random RAND = new Random(System.currentTimeMillis());

    protected void setUp() throws Exception
    {
        super.setUp();

        serialiser = new XmlRecordSerialiser();
        tmpDir = FileSystemUtils.createTempDir();
    }

    protected void tearDown() throws Exception
    {
        removeDirectory(tmpDir);
        serialiser = null;

        super.tearDown();
    }

    public void testSingleRecord() throws IOException
    {
        MutableRecord record = new MutableRecordImpl();
        record.put("a", "a");

        File f = new File(tmpDir, "record.xml");
        assertTrue(f.createNewFile());

        serialiser.serialise(f, record);

        MutableRecord deserialisedRecord = serialiser.deserialise(f, new NoopRecordHandler());

        assertEquals("a", deserialisedRecord.get("a"));
    }

    public void testNestedRecord() throws IOException
    {
        MutableRecord record = new MutableRecordImpl();
        record.put("a", "a");
        MutableRecord childRecord = new MutableRecordImpl();
        childRecord.put("c", "c");
        record.put("b", childRecord);

        File f = new File(tmpDir, "record.xml");
        assertTrue(f.createNewFile());

        serialiser.serialise(f, record);

        MutableRecord deserialisedRecord = serialiser.deserialise(f, new NoopRecordHandler());

        MutableRecord deserialisedChildRecord = (MutableRecord) deserialisedRecord.get("b");
        assertNotNull(deserialisedChildRecord);
        assertEquals("c", deserialisedChildRecord.get("c"));
    }

    public void testEmptyRecordsAreNotTheSameAsNull()
    {
        MutableRecord record = new MutableRecordImpl();

        File f = new File(tmpDir, "record.xml");

        serialiser.serialise(f, record);

        assertNotNull(serialiser.deserialise(f, new NoopRecordHandler()));
    }

    public void testWritingAndReadingMultileRecords() throws IOException
    {
        for (int i = 0; i < 50; i++)
        {
            File f = new File(tmpDir, Integer.toString(i));

            serialiser.serialise(f, createRandomSampleRecord());

            serialiser.deserialise(f, new NoopRecordHandler());
        }
    }

    private MutableRecord createRandomSampleRecord()
    {
        MutableRecord randomSample = new MutableRecordImpl();
        for (int i = 0; i < RAND.nextInt(10); i++)
        {
            randomSample.put(Integer.toString(i), Integer.toString(RAND.nextInt(20)));
        }
        return randomSample;
    }


}
