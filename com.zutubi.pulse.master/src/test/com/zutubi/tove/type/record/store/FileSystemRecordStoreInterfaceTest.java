package com.zutubi.tove.type.record.store;

import com.zutubi.pulse.core.util.FileSystemUtils;
import com.zutubi.tove.transaction.TransactionManager;

import java.io.File;

/**
 *
 *
 */
public class FileSystemRecordStoreInterfaceTest extends AbstractRecordStoreInterfaceTestCase
{
    private File persistentDirectory;

    protected void setUp() throws Exception
    {
        persistentDirectory = FileSystemUtils.createTempDir();

        super.setUp();
    }

    protected void tearDown() throws Exception
    {
        super.tearDown();

        removeDirectory(persistentDirectory);
    }

    public RecordStore createRecordStore() throws Exception
    {
        FileSystemRecordStore recordStore = new FileSystemRecordStore();
        recordStore.setTransactionManager(new TransactionManager());
        recordStore.setPersistenceDirectory(persistentDirectory);
        recordStore.init();
        return recordStore;
    }
}
