package com.zutubi.pulse.restore;

import com.zutubi.pulse.test.PulseTestCase;
import com.zutubi.pulse.util.FileSystemUtils;
import com.zutubi.prototype.type.record.store.RecordStore;
import com.zutubi.prototype.type.record.store.FileSystemRecordStore;
import com.zutubi.prototype.type.record.store.InMemoryRecordStore;
import com.zutubi.prototype.type.record.MutableRecord;
import com.zutubi.prototype.type.record.MutableRecordImpl;
import com.zutubi.prototype.transaction.TransactionManager;

import java.io.File;

/**
 *
 *
 */
public class RecordsArchiveTest extends PulseTestCase
{
    private RecordsArchive archive;
    private InMemoryRecordStore recordStore;

    private File tmp;

    private TransactionManager txnManager;

    protected void setUp() throws Exception
    {
        super.setUp();

        txnManager = new TransactionManager();

        recordStore = new InMemoryRecordStore();
        recordStore.setTransactionManager(txnManager);

        archive = new RecordsArchive();
        archive.setRecordStore(recordStore);

        tmp = FileSystemUtils.createTempDir();
    }

    protected void tearDown() throws Exception
    {
        archive = null;
        recordStore = null;
        txnManager = null;
        
        super.tearDown();
    }

    public void testBackupRestore()
    {
        recordStore.insert("some", new MutableRecordImpl());
        recordStore.insert("some/path", new MutableRecordImpl());
        
        archive.backup(tmp);

        assertNotNull(recordStore.select().get("some"));

        recordStore.delete("some");

        assertNull(recordStore.select().get("some"));

        archive.restore(tmp);

        assertNotNull(recordStore.select().get("some"));
    }
}
