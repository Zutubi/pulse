package com.zutubi.prototype.type.record.store;

import com.zutubi.prototype.transaction.TransactionManager;
import com.zutubi.prototype.transaction.UserTransaction;
import com.zutubi.prototype.type.record.MutableRecord;
import com.zutubi.prototype.type.record.MutableRecordImpl;
import com.zutubi.prototype.type.record.Record;
import com.zutubi.pulse.test.PulseTestCase;
import com.zutubi.pulse.util.FileSystemUtils;
import com.zutubi.util.IOUtils;

import java.io.File;

/**
 *
 *
 */
public class FileSystemRecordStoreTest extends PulseTestCase
{
    private FileSystemRecordStore recordStore = null;
    private File persistentDirectory = null;
    private TransactionManager transactionManager;

    protected void setUp() throws Exception
    {
        super.setUp();

        persistentDirectory = FileSystemUtils.createTempDir();
        transactionManager = new TransactionManager();

        restartRecordStore();
    }

    private void restartRecordStore() throws Exception
    {
        recordStore = new FileSystemRecordStore();
        recordStore.setTransactionManager(transactionManager);
        recordStore.setPersistenceDirectory(persistentDirectory);
        recordStore.init();
    }

    protected void tearDown() throws Exception
    {
        removeDirectory(persistentDirectory);
        transactionManager = null;
        recordStore = null;

        super.tearDown();
    }

    //---( a set of sanity checks of the basic functions. )---

    public void testInsert()
    {
        Record sample = createSampleRecord();
        recordStore.insert("sample", sample);

        Record stored = (Record) recordStore.select().get("sample");
        assertEquals(sample.get("a"), stored.get("a"));
    }

    public void testUpdate()
    {
        MutableRecord sample = createSampleRecord();
        recordStore.insert("sample", sample);

        sample.put("c", "c");

        // ensure that updating the original sample does not update the internal record store.
        Record stored = (Record) recordStore.select().get("sample");
        assertNull(stored.get("c"));

        recordStore.update("sample", sample);
        stored = (Record) recordStore.select().get("sample");
        assertEquals(sample.get("c"), stored.get("c"));
    }

    public void testDelete()
    {
        Record sample = createSampleRecord();
        recordStore.insert("sample", sample);

        recordStore.delete("sample");
        assertNull(recordStore.select().get("sample"));
    }

    //---( check that the index files are correctly generated. )---

    public void testPersistentFiles()
    {
        Record sample = createSampleRecord();
        recordStore.insert("sample", sample);

        assertTrue(new File(persistentDirectory, "index").isFile());
        assertFalse(new File(persistentDirectory, "index.new").isFile());
        assertFalse(new File(persistentDirectory, "index.backup").isFile());

        // check that the journal entry is written to disk
        assertTrue(new File(persistentDirectory, "1").exists());

        assertFalse(new File(persistentDirectory, "2").exists());
        recordStore.update("sample", sample);
        assertTrue(new File(persistentDirectory, "2").exists());

        assertFalse(new File(persistentDirectory, "3").exists());
        recordStore.delete("sample");
        assertTrue(new File(persistentDirectory, "3").exists());
    }

    public void testCompaction() throws Exception
    {
        File snapshot = new File(persistentDirectory, "snapshot");
        File snapshotId = new File(snapshot, "snapshot_id.txt");

        assertFalse(snapshot.exists());

        Record sample = createSampleRecord();

        // insert generates one journal entry.
        recordStore.insert("sample", sample);
        assertFalse(snapshot.exists());

        // compaction removes this journal entry.
        recordStore.compactNow();
        assertTrue(snapshot.exists());
        assertEquals("1", IOUtils.fileToString(new File(snapshot, "snapshot_id.txt")));

        // ensure that the journal entry id is correct
        assertFalse(new File(persistentDirectory, "2").exists());
        recordStore.update("sample", sample);
        assertTrue(new File(persistentDirectory, "2").exists());

        recordStore.compactNow();
        assertEquals("2", IOUtils.fileToString(new File(snapshot, "snapshot_id.txt")));
    }

    public void testCompactionOnRestart() throws Exception
    {
        File snapshot = new File(persistentDirectory, "snapshot");

        Record sample = createSampleRecord();

        // insert generates one journal entry.
        recordStore.insert("sample", sample);
        recordStore.update("sample", sample);
        recordStore.update("sample", sample);

        restartRecordStore();
        assertTrue(snapshot.exists());
        assertTrue(new File(snapshot, "sample").exists());
        assertEquals("3", IOUtils.fileToString(new File(snapshot, "snapshot_id.txt")));

        // restart should cleanup unused journal files.
    }

    public void testMutlipleRestartsAndTxns() throws Exception
    {
        for (int i = 0; i < 20; i++)
        {
            recordStore.insert("path_" + i, createSampleRecord());
            restartRecordStore();
        }
    }

    public void testRestartOnPersistenceDirectoryWithNoData() throws Exception
    {
        restartRecordStore();
    }

    public void testCompactionDuringTransaction() throws Exception
    {
        File snapshot = new File(persistentDirectory, "snapshot");

        Record sample = createSampleRecord();

        // insert generates one journal entry.
        recordStore.insert("sample", sample);

        UserTransaction txn = new UserTransaction(transactionManager);
        txn.begin();

        recordStore.update("sample", sample);

        executeOnSeparateThreadAndWait(new Runnable()
        {
            public void run()
            {
                try
                {
                    recordStore.compactNow();
                }
                catch (Exception e)
                {
                    fail("Exception thrown during test: " + e.getClass().getName() + " " + e.getMessage());
                }
            }
        });

        recordStore.update("sample", sample);
        txn.commit();

        // snapshot should be from before the transaction started and so only contain journal entry 1.
        assertTrue(snapshot.exists());
        assertEquals("1", IOUtils.fileToString(new File(snapshot, "snapshot_id.txt")));
    }

    public void testTransactionRollbackBeforeFirstCommit()
    {
        UserTransaction txn = new UserTransaction(transactionManager);
        txn.begin();

        recordStore.insert("sample", createSampleRecord());

        txn.rollback();
    }

    //---( helper methods. )---

    private MutableRecord createSampleRecord()
    {
        MutableRecord sample = new MutableRecordImpl();
        sample.put("a", "a");
        sample.put("b", "b");
        return sample;
    }
}
