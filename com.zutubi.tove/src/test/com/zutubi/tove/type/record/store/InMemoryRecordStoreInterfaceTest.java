package com.zutubi.tove.type.record.store;

import com.zutubi.tove.transaction.TransactionManager;

public class InMemoryRecordStoreInterfaceTest extends AbstractRecordStoreInterfaceTestCase
{
    public RecordStore createRecordStore()
    {
        InMemoryRecordStore store = new InMemoryRecordStore();
        store.setTransactionManager(new TransactionManager());
        store.init();
        return store;
    }
}
