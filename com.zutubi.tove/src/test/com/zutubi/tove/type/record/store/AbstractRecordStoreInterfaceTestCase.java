package com.zutubi.tove.type.record.store;

import com.zutubi.tove.type.record.MutableRecord;
import com.zutubi.tove.type.record.MutableRecordImpl;
import com.zutubi.tove.type.record.Record;

public abstract class AbstractRecordStoreInterfaceTestCase extends RecordStoreTestCase
{
    private RecordStore recordStore;

    protected void setUp() throws Exception
    {
        super.setUp();

        recordStore = createRecordStore();
    }

    public abstract RecordStore createRecordStore() throws Exception;

    private MutableRecordImpl createSampleRecord()
    {
        MutableRecordImpl sample = new MutableRecordImpl();
        sample.put("a", "a");
        sample.put("b", "b");
        sample.putMeta("c", "c");
        sample.putMeta("d", "d");
        return sample;
    }

    public void testInsert()
    {
        MutableRecord sample = createSampleRecord();
        recordStore.insert("sample", sample);

        Record stored = (Record) recordStore.select().get("sample");
        assertTrue(stored != sample);
        assertRecordsEquals(sample, stored);
    }

    public void testInsertNested()
    {
        MutableRecord sample = createSampleRecord();
        sample.put("nested", createSampleRecord());
        recordStore.insert("sample", sample);

        Record stored = (Record) recordStore.select().get("sample");
        assertTrue(stored != sample);
        assertRecordsEquals(sample, stored);
        assertNotNull(stored.get("nested"));
    }

    public void testInsertParentMustExist()
    {
        try
        {
            MutableRecord sample = createSampleRecord();
            recordStore.insert("non-existant/sample", sample);
            fail();
        }
        catch (IllegalArgumentException e)
        {

        }
    }

    public void testCopyDataOnInsert()
    {
        MutableRecord newRecord = createSampleRecord();
        recordStore.insert("path", newRecord);

        // make an update to the object.
        newRecord.put("aa", "bb");
        newRecord.putMeta("cc", "dd");

        // ensure that the update has not changed the inserted data.
        Record storedRecord = (Record) recordStore.select().get("path");
        assertNull(storedRecord.get("aa"));
        assertNull(storedRecord.getMeta("cc"));
    }


    public void testUpdate()
    {
        MutableRecordImpl newRecord = new MutableRecordImpl();
        newRecord.put("a", "b");
        newRecord.putMeta("c", "d");
        recordStore.insert("path", newRecord);

        MutableRecordImpl updateRecord = new MutableRecordImpl();
        updateRecord.put("aa", "bb");
        updateRecord.putMeta("cc", "dd");
        recordStore.update("path", updateRecord);

        Record storedRecord = (Record) recordStore.select().get("path");
        assertTrue(storedRecord != updateRecord);
        assertRecordsEquals(storedRecord, updateRecord);
    }

    public void testUpdateIsNotNested()
    {
        MutableRecordImpl record = new MutableRecordImpl();
        record.put("a", "b");
        recordStore.insert("path", record);

        record.put("nesting", createSampleRecord());
        recordStore.update("path", record);

        Record storedRecord = (Record) recordStore.select().get("path");
        assertNull(storedRecord.get("nesting"));
    }

    public void testCopyDataOnUpdate()
    {
        MutableRecord newRecord = new MutableRecordImpl();
        newRecord.put("a", "b");
        recordStore.insert("path", newRecord);

        // make an update to the object.
        newRecord.put("c", "d");

        // ensure that the update has not changed the inserted data.
        Record insertedRecord = (Record) recordStore.select().get("path");
        assertNull(insertedRecord.get("c"));
    }

    public void testDelete()
    {
        MutableRecordImpl newRecord = new MutableRecordImpl();
        newRecord.put("a", "b");
        recordStore.insert("path", newRecord);

        Record storedBase = recordStore.select();
        assertNotNull(storedBase.get("path"));

        recordStore.delete("path");

        storedBase = recordStore.select();
        assertNull(storedBase.get("path"));
    }

    // We do not want the values that we have from a select being changed by another process.
    // This is likely to lead to unexpected results.  The select should be a value snapshot in
    // time.
    public void testCopyOnSelect()
    {
        MutableRecordImpl record = new MutableRecordImpl();
        record.put("a", "b");
        recordStore.insert("path", record);

        Record selected = (Record) recordStore.select().get("path");
        assertRecordsEquals(selected, record);

        assertNull(selected.get("c"));

        MutableRecord update = selected.copy(true, true);
        update.put("c", "d");

        recordStore.update("path", update);

        assertNull(selected.get("c"));

        selected = (Record) recordStore.select().get("path");
        assertNotNull(selected.get("c"));
    }

}
