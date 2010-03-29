package com.zutubi.tove.type.record.store;

public class InMemoryRecordStoreInterfaceTest extends AbstractRecordStoreInterfaceTestCase
{
    public RecordStore createRecordStore()
    {
        return new InMemoryRecordStore();
    }
}
