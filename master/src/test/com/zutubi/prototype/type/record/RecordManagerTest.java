package com.zutubi.prototype.type.record;

import junit.framework.TestCase;

import java.util.Map;
import java.util.HashMap;
import java.io.File;

import com.zutubi.pulse.util.FileSystemUtils;

/**
 *
 *
 */
public class RecordManagerTest extends TestCase
{
    private static final long ID_BLOCK_SIZE = 32;
    private File tempDir;
    private RecordManager recordManager;

    protected void setUp() throws Exception
    {
        super.setUp();

        tempDir = FileSystemUtils.createTempDir(getName(), "");
        newRecordManager(ID_BLOCK_SIZE);
    }

    protected void tearDown() throws Exception
    {
        recordManager = null;
        if(!FileSystemUtils.rmdir(tempDir))
        {
            throw new RuntimeException("Unable to remove '" + tempDir + "' because your OS is not brown enough");
        }
        
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

    public void testInsertCreatesId()
    {
        Record record = new MutableRecordImpl();
        recordManager.insert("testpath", record);
        record = recordManager.load("testpath");
        assertEquals(1, record.getID());
    }

    public void testInsertCreatesUniqueIds()
    {
        recordManager.insert("r1", new MutableRecordImpl());
        recordManager.insert("r2", new MutableRecordImpl());
        long id1 = recordManager.load("r1").getID();
        long id2 = recordManager.load("r2").getID();
        assertNotSame(id1, id2);
    }

    public void testIdsAreUniqueAcrossRuns()
    {
        long id = recordManager.allocateId();
        for(int i = 0; i < ID_BLOCK_SIZE * 2 + 5; i++)
        {
            newRecordManager(ID_BLOCK_SIZE);
            for(int j = 0; j < i; j++)
            {
                long nextId = recordManager.allocateId();
                assertNextId(nextId, id);
                id = nextId;
            }
        }
    }

    public void testIncreasingIdBoundary()
    {
        long id = recordManager.allocateId();
        newRecordManager(ID_BLOCK_SIZE + 10);
        assertNextId(recordManager.allocateId(), id);
    }

    public void testDecreasingIdBoundary()
    {
        long id = recordManager.allocateId();
        newRecordManager(ID_BLOCK_SIZE - 1);
        assertNextId(recordManager.allocateId(), id);
    }

    private void assertNextId(long nextId, long id)
    {
        assertTrue("Next id '" + nextId + "' not higher than last '" + id + "'", nextId > id);
    }

    private void newRecordManager(long idBlockSize)
    {
        recordManager = new RecordManager();
        recordManager.setIdBlockSize(idBlockSize);
        recordManager.setRecordSerialiser(new DefaultRecordSerialiser(tempDir));
        recordManager.init();
    }
}
