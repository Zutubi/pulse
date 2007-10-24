package com.zutubi.prototype.type.record;

import com.zutubi.pulse.test.PulseTestCase;
import com.zutubi.pulse.util.FileSystemUtils;

import java.io.File;
import java.io.IOException;

/**
 *
 *
 */
public class XmlRecordSerialiserTest extends PulseTestCase
{
    private XmlRecordSerialiser serialiser;

    private File tmpDir;

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
}
