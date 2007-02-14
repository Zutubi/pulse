package com.zutubi.pulse.prototype.record;

/**
 * <class comment/>
 */
public interface RecordManager
{
    Record load(String path);

    void store(String path, Record record);

    Record delete(String path);
}
