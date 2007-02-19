package com.zutubi.pulse.prototype.record;

/**
 * @deprecated
 */
public interface RecordManager
{
    Record load(String path);

    void store(String path, Record record);

    Record delete(String path);
}
