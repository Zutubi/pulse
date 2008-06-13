package com.zutubi.prototype.type.record.store;

import com.zutubi.prototype.type.record.Record;

/**
 *
 *
 */
public interface RecordStore
{
    /**
     * Insert a new record into the record store at the specified path.  This path can later be
     * used to retrieve that record.
     *
     * The record may itself contain children.
     *
     * The parent of the path at which this record is being inserted must exist.
     *
     * @param path uniquely identifies the record.
     * @param record data being inserted.
     *
     */
    void insert(String path, Record record);

    /**
     * Update the record identified by the specified path.
     *
     * This is NOT a deep update.
     *
     * @param path
     * @param record
     */
    void update(String path, Record record);

    Record delete(String path);

    Record select();

    Record exportRecords();

    void importRecords(Record r);
}
