package com.zutubi.pulse.restore;

import com.zutubi.pulse.test.PulseTestCase;
import com.zutubi.pulse.util.FileSystemUtils;
import com.zutubi.tove.transaction.TransactionManager;
import com.zutubi.tove.type.record.MutableRecordImpl;
import com.zutubi.tove.type.record.RecordQueries;
import com.zutubi.tove.type.record.store.InMemoryRecordStore;

import java.io.File;

/**
 *
 *
 */
public class RecordsArchiveTest extends PulseTestCase
{
    private RecordsArchive recordStoreArchive;
    private InMemoryRecordStore recordStore;

    private File tmp;

    private TransactionManager txnManager;

    protected void setUp() throws Exception
    {
        super.setUp();

        txnManager = new TransactionManager();

        recordStore = new InMemoryRecordStore();
        recordStore.setTransactionManager(txnManager);

        recordStoreArchive = new RecordsArchive();
        recordStoreArchive.setRecordStore(recordStore);

        tmp = FileSystemUtils.createTempDir();
    }

    protected void tearDown() throws Exception
    {
        recordStoreArchive = null;
        recordStore = null;
        txnManager = null;
        
        super.tearDown();
    }

    public void testEmptyStore()
    {
        recordStoreArchive.backup(tmp);
        assertNull(RecordQueries.getQueries(recordStore).select("some"));

        recordStore.insert("some", new MutableRecordImpl());
        assertNotNull(RecordQueries.getQueries(recordStore).select("some"));

        recordStoreArchive.restore(tmp);
        assertNull(RecordQueries.getQueries(recordStore).select("some"));
    }

    public void testSimpleDataBackup()
    {
        recordStore.insert("some", new MutableRecordImpl());
        recordStore.insert("some/path", new MutableRecordImpl());
        
        recordStoreArchive.backup(tmp);
        assertNotNull(RecordQueries.getQueries(recordStore).select("some/path"));

        recordStore.delete("some");
        assertNull(RecordQueries.getQueries(recordStore).select("some/path"));

        recordStoreArchive.restore(tmp);
        assertNotNull(RecordQueries.getQueries(recordStore).select("some/path"));
    }
}
