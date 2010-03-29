package com.zutubi.tove.type.record.store;

import com.zutubi.tove.transaction.TransactionManager;
import com.zutubi.tove.transaction.UserTransaction;
import com.zutubi.tove.type.record.MutableRecord;
import com.zutubi.tove.type.record.MutableRecordImpl;
import com.zutubi.tove.type.record.Record;

/**
 *
 *
 */
public class InMemoryRecordStoreTest extends RecordStoreTestCase
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

    public void testSelect()
    {
        assertNotNull(recordStore.select());
        assertEquals(0, recordStore.select().size());
    }

    public void testInsertIsolation()
    {
        MutableRecordImpl newRecord = new MutableRecordImpl();
        newRecord.put("a", "b");
        recordStore.insert("path", newRecord);

        executeOnSeparateThreadAndWait(new Runnable()
        {
            public void run()
            {
                assertNull(recordStore.select().get("path"));
            }
        });
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

        executeOnSeparateThreadAndWait(new Runnable()
        {
            public void run()
            {
                Record r = (Record) recordStore.select().get("path");
                assertNull(r.get("c"));
            }
        });
    }

    public void testDeleteIsolation()
    {
        MutableRecordImpl insert = new MutableRecordImpl();
        insert.put("a", "b");
        recordStore.insert("path", insert);

        transaction.commit();
        transaction.begin();

        recordStore.delete("path");

        executeOnSeparateThreadAndWait(new Runnable()
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

        executeOnSeparateThreadAndWait(new Runnable()
        {
            public void run()
            {
                assertNull(recordStore.select().get("path"));
            }
        });

        transaction.commit();

        executeOnSeparateThreadAndWait(new Runnable()
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
        executeOnSeparateThreadAndWait(new Runnable()
        {
            public void run()
            {
                assertNull(recordStore.select().get("path"));
            }
        });

        transaction.rollback();

        assertNull(recordStore.select().get("path"));
        executeOnSeparateThreadAndWait(new Runnable()
        {
            public void run()
            {
                assertNull(recordStore.select().get("path"));
            }
        });
    }

    public void testChangesOutsideATransaction()
    {
        transactionManager.commit();

        MutableRecordImpl newRecord = new MutableRecordImpl();
        newRecord.put("a", "b");
        recordStore.insert("path", newRecord);

        // the change is available immediately.
        executeOnSeparateThreadAndWait(new Runnable()
        {
            public void run()
            {
                assertNotNull(recordStore.select().get("path"));
            }
        });
    }

    public void testCopyOnSelectWithinSingleTransaction()
    {
        MutableRecordImpl record = new MutableRecordImpl();
        record.put("a", "b");
        recordStore.insert("path", record);

        Record selected = (Record) recordStore.select().get("path");
        assertRecordsEquals(selected, record);

        assertNull(selected.get("c"));

        MutableRecord update = selected.copy(true, true);
        update.put("c", "d");

        recordStore.update("path", update);

        assertNull(selected.get("c"));

        selected = (Record) recordStore.select().get("path");
        assertNotNull(selected.get("c"));
    }

    public void testUpdateRemovesValueConcurrentModificationException()
    {
        MutableRecordImpl record = new MutableRecordImpl();
        record.put("a", "b");
        record.put("b", "b");
        record.putMeta("c", "b");
        record.putMeta("d", "b");
        recordStore.insert("path", record);

        Record stored = (Record) recordStore.select().get("path");

        MutableRecordImpl update = new MutableRecordImpl();
        update.setHandle(stored.getHandle());
        
        recordStore.update("path", update);
    }
}
