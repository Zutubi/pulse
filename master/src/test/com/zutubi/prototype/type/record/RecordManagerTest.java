package com.zutubi.prototype.type.record;

import junit.framework.TestCase;

/**
 *
 *
 */
public class RecordManagerTest extends TestCase
{
    private RecordManager recordManager;

    protected void setUp() throws Exception
    {
        super.setUp();

        recordManager = new RecordManager();
    }

    protected void tearDown() throws Exception
    {
        recordManager = null;

        super.tearDown();
    }

    public void testStore()
    {
        recordManager.store("hello", new Record());

        Record record = new Record();
        record.put("key", "value");
        recordManager.store("hello/world", record);

        Record hello = recordManager.load("hello");
        assertNotNull(hello.get("world"));

        try
        {
            recordManager.store("hello/world/key", new Record());
            fail();
        }
        catch (IllegalArgumentException e)
        {
        }
    }

    public void testLoad()
    {
        Record record = new Record();
        record.putMeta("key", "value");

        recordManager.store("hello/world", record);
        assertNull(recordManager.load("hello/moon"));
        assertNull(recordManager.load("hello/world/key"));
        assertNotNull(recordManager.load("hello/world"));
    }

    public void testDelete()
    {
        Record record = new Record();
        record.putMeta("key", "value");

        recordManager.store("hello/world", record);

        assertNull(recordManager.delete("hello/moon"));
        assertNull(recordManager.delete("hello/world/key"));
        assertNotNull(recordManager.delete("hello/world"));
        assertNull(recordManager.delete("hello/world"));
    }

    public void testMetaProperties()
    {
        Record record = new Record();
        record.putMeta("key", "value");
        recordManager.store("another/path/to/record", record);

        Record loadedRecord = recordManager.load("another/path/to/record");
        assertEquals(record.getMeta("key"), loadedRecord.getMeta("key"));
    }

    public void testTrimmingPath()
    {
        Record record = new Record();
        record.putMeta("key", "value");

        recordManager.store("another", record);

        assertNotNull(recordManager.load("/another"));
        assertNotNull(recordManager.load("another"));
        assertNotNull(recordManager.load("another/"));
    }
}
