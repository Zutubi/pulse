package com.zutubi.prototype.type.record.store;

import com.zutubi.prototype.transaction.TransactionManager;
import com.zutubi.prototype.transaction.UserTransaction;
import com.zutubi.prototype.type.record.MutableRecord;
import com.zutubi.prototype.type.record.MutableRecordImpl;
import com.zutubi.pulse.test.PulseTestCase;
import com.zutubi.pulse.util.FileSystemUtils;

import java.io.File;

/**
 *
 *
 */
public class FileSystemRecordStoreTest extends PulseTestCase
{
    private FileSystemRecordStore recordStore;

    private TransactionManager transactionManager;

    private UserTransaction transaction;

    private File persistentDir;

    protected void setUp() throws Exception
    {
        super.setUp();

        persistentDir = FileSystemUtils.createTempDir();

        transactionManager = new TransactionManager();
        transaction = new UserTransaction(transactionManager);
        transaction.begin();

        recordStore = new FileSystemRecordStore();
        recordStore.setTransactionManager(transactionManager);
        recordStore.setPersistenceDir(persistentDir);
        recordStore.init();
    }

    protected void tearDown() throws Exception
    {
        transactionManager = null;
        
        removeDirectory(persistentDir);

        super.tearDown();
    }

    public void testInsert()
    {
        MutableRecordImpl newRecord = new MutableRecordImpl();
        newRecord.put("a", "b");

        recordStore.insert("path", newRecord);

        // assert that it can be selected.
        MutableRecord inserted = (MutableRecord) recordStore.select().get("path");
        assertNotNull(inserted);

        // assert that it is not visible to other threads.
        executeOnSeparateThread(new Runnable()
        {
            public void run()
            {
                assertNull(recordStore.select().get("path"));
            }
        });

        // commit.
        transaction.commit();

        // assert that it is available to other threads.
        executeOnSeparateThread(new Runnable()
        {
            public void run()
            {
                assertNotNull(recordStore.select().get("path"));
            }
        });

        // restart
        recordStore = new FileSystemRecordStore();
        recordStore.setPersistenceDir(persistentDir);
        recordStore.init();

        // assert that it was successfully persisted.
        assertNotNull(recordStore.select().get("path"));
    }

    public void testDelete()
    {
        // setup.
        MutableRecordImpl newRecord = new MutableRecordImpl();
        newRecord.put("a", "b");
        recordStore.insert("path", newRecord);

        transaction.commit();
        transaction.begin();
        
        assertNotNull(recordStore.delete("path"));

        // assert that it can be selected.
        MutableRecord deleted = (MutableRecord) recordStore.select().get("path");
        assertNull(deleted);

        // assert that it is not visible to other threads.
        executeOnSeparateThread(new Runnable()
        {
            public void run()
            {
                assertNotNull(recordStore.select().get("path"));
            }
        });

        // commit.
        transaction.commit();

        deleted = (MutableRecord) recordStore.select().get("path");
        assertNull(deleted);

        // assert that it is available to other threads.
        executeOnSeparateThread(new Runnable()
        {
            public void run()
            {
                assertNull(recordStore.select().get("path"));
            }
        });

        // restart
        recordStore = new FileSystemRecordStore();
        recordStore.setPersistenceDir(persistentDir);
        recordStore.init();

        // assert that it was successfully persisted.
        assertNull(recordStore.select().get("path"));
    }

    public void testRollback()
    {
        // start the transaction.
        MutableRecordImpl newRecord = new MutableRecordImpl();
        newRecord.put("a", "b");
        recordStore.insert("path", newRecord);

        // rollback.
        transaction.rollback();

        // ensure that we have rolled back to the blank state.
        assertNull(recordStore.select().get("path"));

        // restart
        recordStore = new FileSystemRecordStore();
        recordStore.setPersistenceDir(persistentDir);
        recordStore.init();

        assertNull(recordStore.select().get("path"));
    }

    // ensure that changes made outside the scope of a transaction are handled correctly.
    public void testAutoCommit()
    {
        // close the default transaction.
        transaction.commit();

        // update the data.
        MutableRecordImpl newRecord = new MutableRecordImpl();
        newRecord.put("a", "b");

        recordStore.insert("path", newRecord);

        // restart
        recordStore = new FileSystemRecordStore();
        recordStore.setPersistenceDir(persistentDir);
        recordStore.init();

        // assert that it was successfully persisted.
        assertNotNull(recordStore.select().get("path"));
    }
}
