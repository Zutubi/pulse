package com.zutubi.prototype.type.record.store;

import com.zutubi.prototype.type.record.Record;

/**
 *
 *
 */
public interface RecordStore
{
    Record insert(String path, Record record);

    Record update(String path, Record record);

    Record delete(String path);

    Record select();
}
