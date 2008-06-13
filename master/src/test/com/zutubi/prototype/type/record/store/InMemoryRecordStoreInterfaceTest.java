package com.zutubi.prototype.type.record.store;

/**
 *
 *
 */
public class InMemoryRecordStoreInterfaceTest extends AbstractRecordStoreInterfaceTestCase
{
    public RecordStore createRecordStore()
    {
        return new InMemoryRecordStore();
    }
}
