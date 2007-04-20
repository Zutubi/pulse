package com.zutubi.prototype.type.record;

import junit.framework.TestCase;

import java.util.Map;
import java.util.HashMap;

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
        recordManager.setRecordSerialiser(new MockRecordSerialiser());        
    }

    protected void tearDown() throws Exception
    {
        recordManager = null;

        super.tearDown();
    }

    public void testInsert()
    {
        recordManager.insert("hello", new MutableRecordImpl());

        MutableRecordImpl record = new MutableRecordImpl();
        record.put("key", "value");
        recordManager.insert("hello/world", record);

        Record hello = recordManager.load("hello");
        assertNotNull(hello.get("world"));
    }

    public void testLoad()
    {
        MutableRecordImpl record = new MutableRecordImpl();
        record.putMeta("key", "value");

        recordManager.insert("hello", new MutableRecordImpl());
        recordManager.insert("hello/world", record);
        assertNull(recordManager.load("hello/moon"));
        assertNull(recordManager.load("hello/world/key"));
        assertNotNull(recordManager.load("hello/world"));
    }

    public void testDelete()
    {
        MutableRecordImpl record = new MutableRecordImpl();
        record.putMeta("key", "value");

        recordManager.insert("hello", new MutableRecordImpl());
        recordManager.insert("hello/world", record);

        assertNull(recordManager.delete("hello/moon"));
        assertNull(recordManager.delete("hello/world/key"));
        assertNotNull(recordManager.delete("hello/world"));
        assertNull(recordManager.delete("hello/world"));
    }

    public void testMetaProperties()
    {
        MutableRecordImpl record = new MutableRecordImpl();
        record.putMeta("key", "value");
        recordManager.insert("path", record);

        Record loadedRecord = recordManager.load("path");
        assertEquals(record.getMeta("key"), loadedRecord.getMeta("key"));
    }

    public void testTrimmingPath()
    {
        MutableRecordImpl record = new MutableRecordImpl();
        record.putMeta("key", "value");

        recordManager.insert("another", record);

        assertNotNull(recordManager.load("/another"));
        assertNotNull(recordManager.load("another"));
        assertNotNull(recordManager.load("another/"));
    }

    public void testCopy()
    {
        MutableRecordImpl original = new MutableRecordImpl();
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

    public void testCopyOnInsert()
    {
        MutableRecordImpl original = new MutableRecordImpl();
        original.put("key", "value");

        recordManager.insert("path", original);

        // update the external record.
        original.put("key", "changedValue");

        Record storedRecord = recordManager.load("path");
        assertEquals("value", storedRecord.get("key"));
    }

    public void testContainsRecord()
    {
        assertFalse(recordManager.containsRecord("a"));
        recordManager.insert("a", new MutableRecordImpl());
        assertTrue(recordManager.containsRecord("a"));
    }

    public void testLoadAll()
    {
        // setup test data.
        recordManager.insert("a", new MutableRecordImpl());
        recordManager.insert("a/b", new MutableRecordImpl());
        recordManager.insert("a/b/c", new MutableRecordImpl());

        assertLoadedRecordCount("a", 1);
        assertLoadedRecordCount("a/b", 1);
        assertLoadedRecordCount("a/b/c", 1);
        assertLoadedRecordCount("a/b/c/d", 0);

        assertLoadedRecordCount("a/*/c", 1);
        assertLoadedRecordCount("*/*/*", 1);

        recordManager.insert("a/b/d", new MutableRecordImpl());
        assertLoadedRecordCount("a/b/*", 2);
    }

    public void testLoadAllHandlesNullInput()
    {
        try
        {
            recordManager.loadAll(null, new HashMap<String, Record>());
            fail();
        }
        catch (IllegalArgumentException e)
        {
            // noop.
        }
    }

    private void assertLoadedRecordCount(String path, int count)
    {
        Map<String, Record> records = new HashMap<String, Record>();
        recordManager.loadAll(path, records);
        assertEquals(count, records.size());
    }

    public void testLoadAllEmptyPath()
    {
        try
        {
            recordManager.loadAll("", new HashMap<String, Record>());
            fail();
        }
        catch (IllegalArgumentException e)
        {
            // noop.
        }
    }

    public void testLoadEmptyPath()
    {
        try
        {
            recordManager.load("");
            fail();
        }
        catch (IllegalArgumentException e)
        {
            // noop.
        }
    }

    public void testLoadHandlesNullInput()
    {
        try
        {
            recordManager.load(null);
            fail();
        }
        catch (IllegalArgumentException e)
        {
            // noop.
        }
    }

    public void testInsertAtEmptyString()
    {
        try
        {
            recordManager.insert("", new MutableRecordImpl());
            fail();
        }
        catch (IllegalArgumentException e)
        {
            // noop.
        }

    }
}
