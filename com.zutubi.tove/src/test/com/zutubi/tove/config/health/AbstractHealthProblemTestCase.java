package com.zutubi.tove.config.health;

import com.zutubi.events.DefaultEventManager;
import com.zutubi.tove.transaction.TransactionManager;
import com.zutubi.tove.type.record.RecordManager;
import com.zutubi.tove.type.record.store.InMemoryRecordStore;
import com.zutubi.util.junit.ZutubiTestCase;

public abstract class AbstractHealthProblemTestCase extends ZutubiTestCase
{
    protected RecordManager recordManager;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();

        TransactionManager transactionManager = new TransactionManager();

        InMemoryRecordStore recordStore = new InMemoryRecordStore();
        recordStore.setTransactionManager(transactionManager);
        recordStore.init();

        recordManager = new RecordManager();
        recordManager.setEventManager(new DefaultEventManager());
        recordManager.setRecordStore(recordStore);
        recordManager.setTransactionManager(transactionManager);
        recordManager.init();
    }
}
