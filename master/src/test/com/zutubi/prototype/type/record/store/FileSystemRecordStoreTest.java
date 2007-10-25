package com.zutubi.prototype.type.record.store;

import com.zutubi.prototype.transaction.TransactionManager;
import com.zutubi.prototype.transaction.UserTransaction;
import com.zutubi.prototype.type.record.MutableRecord;
import com.zutubi.prototype.type.record.MutableRecordImpl;
import com.zutubi.prototype.type.record.Record;
import com.zutubi.prototype.type.record.DefaultRecordSerialiser;
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

    public void testInsertPersistence() throws Exception
    {
        Record sample = createSampleRecord();
        recordStore.insert("sample", sample);

        restartRecordStore();

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

        // now update the record store.
        recordStore.update("sample", sample);
        stored = (Record) recordStore.select().get("sample");
        assertEquals(sample.get("c"), stored.get("c"));
    }

    public void testUpdatePersistence() throws Exception
    {
        MutableRecord sample = createSampleRecord();
        recordStore.insert("sample", sample);
        recordStore.update("sample", sample);
        
        restartRecordStore();

        Record stored = (Record) recordStore.select().get("sample");
        assertEquals(sample.get("c"), stored.get("c"));
    }

    public void testDelete()
    {
        Record sample = createSampleRecord();
        recordStore.insert("sample", sample);

        recordStore.delete("sample");
        assertNull(recordStore.select().get("sample"));
    }

    public void testDeletePersistence() throws Exception
    {
        Record sample = createSampleRecord();
        recordStore.insert("sample", sample);
        recordStore.delete("sample");

        restartRecordStore();

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
        assertTrue(new File(persistentDirectory, "1").exists());

        // compaction removes this journal entry.
        recordStore.compactNow();
        assertTrue(snapshot.exists());
        assertEquals("1", IOUtils.fileToString(snapshotId));
        assertFalse(new File(persistentDirectory, "1").exists());

        // ensure that the journal entry id is correct
        assertFalse(new File(persistentDirectory, "2").exists());
        recordStore.update("sample", sample);
        assertTrue(new File(persistentDirectory, "2").exists());

        recordStore.compactNow();
        assertEquals("2", IOUtils.fileToString(snapshotId));
        assertFalse(new File(persistentDirectory, "2").exists());

        // check that the snapshot is as expected.
        DefaultRecordSerialiser serialiser = new DefaultRecordSerialiser(snapshot);
        Record snapshotRecord = serialiser.deserialise("sample");
        assertEquals(sample, snapshotRecord);

        // check that the file system is cleaned up.
        assertEquals(3, persistentDirectory.list().length);
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
        for (int i = 0; i < 10; i++)
        {
            recordStore.insert("path_" + i, createSampleRecord());
            restartRecordStore();
        }
    }

    // Testing some of the boundry conditions.
    public void testRestartOnPersistenceDirectoryWithNoData() throws Exception
    {
        Record base = recordStore.select();
        assertNotNull(base);
        assertEquals(0, base.keySet().size());

        restartRecordStore();

        base = recordStore.select();
        assertNotNull(base);
        assertEquals(0, base.keySet().size());
    }

    public void testCompactionDuringCommitTransaction() throws Exception
    {
        File snapshot = new File(persistentDirectory, "snapshot");

        Record sample = createSampleRecord();

        // insert generates one journal entry.
        recordStore.insert("sample", sample);

        UserTransaction txn = new UserTransaction(transactionManager);
        txn.begin();

        MutableRecord anotherSample = createSampleRecord();
        anotherSample.put("d", "d");
        recordStore.insert("another", anotherSample);

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

        recordStore.update("sample", anotherSample);
        txn.commit();

        // snapshot should be from before the transaction started and so only contain journal entry 1.
        assertTrue(snapshot.exists());
        assertEquals("1", IOUtils.fileToString(new File(snapshot, "snapshot_id.txt")));

        // ensure that the snapshot is of the data from before the second transaction.
        DefaultRecordSerialiser serialiser = new DefaultRecordSerialiser(snapshot);
        Record snapshotRecord = serialiser.deserialise("sample");
        assertEquals(sample, snapshotRecord);
    }

    public void testCompactionDuringRollbackTransaction() throws Exception
    {
        File snapshot = new File(persistentDirectory, "snapshot");

        Record sample = createSampleRecord();

        // insert generates one journal entry.
        recordStore.insert("sample", sample);

        UserTransaction txn = new UserTransaction(transactionManager);
        txn.begin();

        MutableRecord anotherSample = createSampleRecord();
        anotherSample.put("d", "d");
        recordStore.insert("another", anotherSample);

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

        recordStore.update("sample", anotherSample);
        txn.rollback();

        // snapshot should be from before the transaction started and so only contain journal entry 1.
        assertTrue(snapshot.exists());
        assertEquals("1", IOUtils.fileToString(new File(snapshot, "snapshot_id.txt")));

        // ensure that the snapshot is of the data from before the second transaction.
        DefaultRecordSerialiser serialiser = new DefaultRecordSerialiser(snapshot);
        Record snapshotRecord = serialiser.deserialise("sample");
        assertEquals(sample, snapshotRecord);
    }

    public void testTransactionRollback()
    {
        UserTransaction txn = new UserTransaction(transactionManager);
        txn.begin();

        recordStore.insert("sample", createSampleRecord());

        txn.rollback();

        assertNull(recordStore.select().get("sample"));
        assertFalse(new File(persistentDirectory, "index").exists());
        assertFalse(new File(persistentDirectory, "1").exists());

        txn.begin();

        recordStore.insert("sample", createSampleRecord());

        txn.commit();

        assertTrue(new File(persistentDirectory, "index").exists());
        assertTrue(new File(persistentDirectory, "2").exists());
        assertNotNull(recordStore.select().get("sample"));

        txn.begin();

        recordStore.insert("another", createSampleRecord());

        txn.rollback();

        assertTrue(new File(persistentDirectory, "index").exists());
        assertTrue(new File(persistentDirectory, "2").exists());
        assertFalse(new File(persistentDirectory, "3").exists());
        assertNull(recordStore.select().get("another"));
    }

    public void testTransactionCommit()
    {
        UserTransaction txn = new UserTransaction(transactionManager);
        txn.begin();

        recordStore.insert("sample", createSampleRecord());

        assertFalse(new File(persistentDirectory, "index").exists());
        assertFalse(new File(persistentDirectory, "1").exists());

        txn.commit();

        assertTrue(new File(persistentDirectory, "index").exists());
        assertTrue(new File(persistentDirectory, "1").exists());
    }

    // test restart recovery when shutdown occured during a transaction. 

    //---( helper methods. )---

    private MutableRecord createSampleRecord()
    {
        MutableRecord sample = new MutableRecordImpl();
        sample.put("a", "a");
        sample.put("b", "b");
        return sample;
    }

    private void assertEquals(Record expected, Record actual)
    {
        assertEquals(expected.size(), actual.size());

        assertEquals(expected.keySet(), actual.keySet());
        for (String key : expected.keySet())
        {
            assertEquals(expected.get(key), actual.get(key));
        }

        assertEquals(expected.metaKeySet(), actual.metaKeySet());
        for (String key : expected.metaKeySet())
        {
            assertEquals(expected.getMeta(key), actual.getMeta(key));
        }

        assertEquals(expected.nestedKeySet(), actual.nestedKeySet());
        for (String key : expected.nestedKeySet())
        {
            assertEquals((Record)expected.get(key), (Record)actual.get(key));
        }
    }
}
