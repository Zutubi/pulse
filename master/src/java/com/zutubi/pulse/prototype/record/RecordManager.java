package com.zutubi.pulse.prototype.record;

import com.zutubi.pulse.prototype.Scope;

/**
 * <class comment/>
 */
public interface RecordManager
{
    Record load(Scope scope, String path);

    Record load(Scope scope, String path, RecordFactory factory);

    void store(Record record);

    void delete(Record record);
}
