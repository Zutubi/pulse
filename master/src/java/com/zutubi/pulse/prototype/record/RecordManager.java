package com.zutubi.pulse.prototype.record;

import com.zutubi.pulse.prototype.Scope;

/**
 * <class comment/>
 */
public interface RecordManager
{
    Record load(Scope scope, String path);

    void store(Record record);

    void delete(Record record);
}
