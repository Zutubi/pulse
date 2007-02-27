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
        recordManager.insert("hello", new MutableRecord());

        MutableRecord record = new MutableRecord();
        record.put("key", "value");
        recordManager.insert("hello/world", record);

        Record hello = recordManager.load("hello");
        assertNotNull(hello.get("world"));

        try
        {
            recordManager.insert("hello/world/key", new MutableRecord());
            fail();
        }
        catch (IllegalArgumentException e)
        {
        }
    }

    public void testLoad()
    {
        MutableRecord record = new MutableRecord();
        record.putMeta("key", "value");

        recordManager.insert("hello/world", record);
        assertNull(recordManager.load("hello/moon"));
        assertNull(recordManager.load("hello/world/key"));
        assertNotNull(recordManager.load("hello/world"));
    }

    public void testDelete()
    {
        MutableRecord record = new MutableRecord();
        record.putMeta("key", "value");

        recordManager.insert("hello/world", record);

        assertNull(recordManager.delete("hello/moon"));
        assertNull(recordManager.delete("hello/world/key"));
        assertNotNull(recordManager.delete("hello/world"));
        assertNull(recordManager.delete("hello/world"));
    }

    public void testMetaProperties()
    {
        MutableRecord record = new MutableRecord();
        record.putMeta("key", "value");
        recordManager.insert("path/to/record", record);

        Record loadedRecord = recordManager.load("path/to/record");
        assertEquals(record.getMeta("key"), loadedRecord.getMeta("key"));
    }

    public void testTrimmingPath()
    {
        MutableRecord record = new MutableRecord();
        record.putMeta("key", "value");

        recordManager.insert("another", record);

        assertNotNull(recordManager.load("/another"));
        assertNotNull(recordManager.load("another"));
        assertNotNull(recordManager.load("another/"));
    }

    public void testCopy()
    {
        MutableRecord original = new MutableRecord();
        original.put("key", "value");

        recordManager.insert("sourcePath", original);
        assertNull(recordManager.load("destinationPath"));
        recordManager.copy("sourcePath", "destinationPath");

        Record copy = recordManager.load("destinationPath");
        assertNotNull(copy);
        assertEquals(original.get("key"), copy.get("key"));

        // ensure that changing the original does not change the copy
        original.put("anotherKey", "anotherValue");
        recordManager.insert("sourcePath", original);

        copy = recordManager.load("destinationPath");
        assertFalse(copy.containsKey("anotherKey"));
    }

    public void testCopyOnStore()
    {
        MutableRecord original = new MutableRecord();
        original.put("key", "value");

        recordManager.insert("path", original);

        // update the external record.
        original.put("key", "changedValue");

        Record storedRecord = recordManager.load("path");
        assertEquals("value", storedRecord.get("key"));
    }
}
