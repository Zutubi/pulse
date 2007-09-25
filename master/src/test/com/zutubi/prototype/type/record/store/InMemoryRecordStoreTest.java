package com.zutubi.prototype.type.record.store;

import com.zutubi.prototype.transaction.TransactionManager;
import com.zutubi.prototype.transaction.UserTransaction;
import com.zutubi.prototype.type.record.MutableRecord;
import com.zutubi.prototype.type.record.MutableRecordImpl;
import com.zutubi.prototype.type.record.Record;
import com.zutubi.pulse.test.PulseTestCase;

/**
 *
 *
 */
public class InMemoryRecordStoreTest extends PulseTestCase
{
    private InMemoryRecordStore recordStore;
    private TransactionManager transactionManager;
    private UserTransaction transaction;

    protected void setUp() throws Exception
    {
        super.setUp();

        transactionManager = new TransactionManager();
        transactionManager.begin();
        
        transaction = new UserTransaction(transactionManager);

        MutableRecordImpl base = new MutableRecordImpl();
        recordStore = new InMemoryRecordStore(base);

        recordStore.setTransactionManager(transactionManager);
    }

    protected void tearDown() throws Exception
    {
        transaction = null;
        transactionManager = null;
        recordStore = null;

        super.tearDown();
    }

    public void testInsertDataStorage()
    {
        MutableRecordImpl newRecord = new MutableRecordImpl();
        newRecord.put("a", "b");
        newRecord.putMeta("c", "d");
        recordStore.insert("path", newRecord);

        Record storedRecord = (Record) recordStore.select().get("path");
        assertEquals("b", storedRecord.get("a"));
        assertEquals("d", storedRecord.getMeta("c"));
    }

    public void testUpdateDataStorage()
    {
        MutableRecordImpl newRecord = new MutableRecordImpl();
        newRecord.put("a", "b");
        newRecord.putMeta("c", "d");
        recordStore.insert("path", newRecord);

        MutableRecordImpl updateRecord = new MutableRecordImpl();
        updateRecord.put("aa", "bb");
        updateRecord.putMeta("cc", "dd");
        recordStore.update("path", updateRecord);

        Record storedRecord = (Record) recordStore.select().get("path");
        assertNull(storedRecord.get("a"));
        assertNull(storedRecord.getMeta("c"));
        assertEquals("bb", storedRecord.get("aa"));
        assertEquals("dd", storedRecord.getMeta("cc"));
    }

    public void testSelect()
    {
        assertNotNull(recordStore.select());
        assertEquals(0, recordStore.select().size());
    }

    public void testBasicInsert()
    {
        MutableRecordImpl newRecord = new MutableRecordImpl();
        newRecord.put("a", "b");
        recordStore.insert("path", newRecord);

        // check that the editing thread sees the expected changes/
        Record storedRecord = (Record) recordStore.select().get("path");
        assertNotNull(storedRecord);
        assertEquals("b", storedRecord.get("a"));
    }

    public void testInsertIsolation()
    {
        MutableRecordImpl newRecord = new MutableRecordImpl();
        newRecord.put("a", "b");
        recordStore.insert("path", newRecord);

        executeOnSeparateThread(new Runnable()
        {
            public void run()
            {
                assertNull(recordStore.select().get("path"));
            }
        });
    }

    public void testCopyDataOnInsert()
    {
        MutableRecord newRecord = new MutableRecordImpl();
        newRecord.put("a", "b");
        recordStore.insert("path", newRecord);
        
        // make an update to the object.
        newRecord.put("c", "d");

        // ensure that the update has not changed the inserted data.
        Record insertedRecord = (Record) recordStore.select().get("path");
        assertNull(insertedRecord.get("c"));
    }

    public void testBasicUpdate()
    {
        MutableRecordImpl newRecord = new MutableRecordImpl();
        newRecord.put("a", "b");
        recordStore.insert("path", newRecord);

        Record storedBase = recordStore.select();
        Record r = (Record) storedBase.get("path");
        assertNull(r.get("b"));

        newRecord.put("b", "c");
        recordStore.update("path", newRecord);

        storedBase = recordStore.select();
        r = (Record) storedBase.get("path");
        assertEquals("c", r.get("b"));
    }

    public void testUpdateIsolation()
    {
        MutableRecordImpl insert = new MutableRecordImpl();
        insert.put("a", "b");
        recordStore.insert("path", insert);

        transaction.commit();
        transaction.begin();

        MutableRecordImpl update = new MutableRecordImpl();
        update.put("c", "d");
        recordStore.update("path", update);

        executeOnSeparateThread(new Runnable()
        {
            public void run()
            {
                Record r = (Record) recordStore.select().get("path");
                assertNull(r.get("c"));
            }
        });
    }

    public void testCopyDataOnUpdate()
    {
        MutableRecord newRecord = new MutableRecordImpl();
        newRecord.put("a", "b");
        recordStore.insert("path", newRecord);

        // make an update to the object.
        newRecord.put("c", "d");

        // ensure that the update has not changed the inserted data.
        Record insertedRecord = (Record) recordStore.select().get("path");
        assertNull(insertedRecord.get("c"));
    }
    
    public void testDelete()
    {
        MutableRecordImpl newRecord = new MutableRecordImpl();
        newRecord.put("a", "b");
        recordStore.insert("path", newRecord);

        Record storedBase = recordStore.select();
        assertNotNull(storedBase.get("path"));

        recordStore.delete("path");

        storedBase = recordStore.select();
        assertNull(storedBase.get("path"));
    }

    public void testDeleteIsolation()
    {
        MutableRecordImpl insert = new MutableRecordImpl();
        insert.put("a", "b");
        recordStore.insert("path", insert);

        transaction.commit();
        transaction.begin();

        recordStore.delete("path");

        executeOnSeparateThread(new Runnable()
        {
            public void run()
            {
                assertNotNull(recordStore.select().get("path"));
            }
        });
    }

    public void testCommit()
    {
        MutableRecordImpl newRecord = new MutableRecordImpl();
        newRecord.put("a", "b");
        recordStore.insert("path", newRecord);

        executeOnSeparateThread(new Runnable()
        {
            public void run()
            {
                assertNull(recordStore.select().get("path"));
            }
        });

        transaction.commit();

        executeOnSeparateThread(new Runnable()
        {
            public void run()
            {
                assertNotNull(recordStore.select().get("path"));
            }
        });
    }

    public void testRollback()
    {
        MutableRecordImpl newRecord = new MutableRecordImpl();
        newRecord.put("a", "b");
        recordStore.insert("path", newRecord);

        assertNotNull(recordStore.select().get("path"));
        executeOnSeparateThread(new Runnable()
        {
            public void run()
            {
                assertNull(recordStore.select().get("path"));
            }
        });

        transaction.rollback();

        assertNull(recordStore.select().get("path"));
        executeOnSeparateThread(new Runnable()
        {
            public void run()
            {
                assertNull(recordStore.select().get("path"));
            }
        });
    }

    public void testMultipleCommitsWithoutChange()
    {
        // This test may look a little unusual.  However, it is designed to catch a problem that
        // existed in the original implementation of the InMemoryRecordStore.commit, a problem that
        // did not show up when being run via the transaction manager interface.

        MutableRecordImpl newRecord = new MutableRecordImpl();
        newRecord.put("a", "b");
        recordStore.insert("path", newRecord);
        transaction.commit();

        assertNotNull(recordStore.select().get("path"));

        recordStore.prepare();
        recordStore.commit();

        assertNotNull(recordStore.select().get("path"));

        // now lets test this using the 'transaction' interface
        transaction.begin();
        transaction.commit();
    }

    public void testChangesOutsideATransaction()
    {
        transactionManager.commit();

        MutableRecordImpl newRecord = new MutableRecordImpl();
        newRecord.put("a", "b");
        recordStore.insert("path", newRecord);

        // the change is available immediately.
        executeOnSeparateThread(new Runnable()
        {
            public void run()
            {
                assertNotNull(recordStore.select().get("path"));
            }
        });

    }
}
