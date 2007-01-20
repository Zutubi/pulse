package com.zutubi.pulse.prototype;

import com.zutubi.pulse.prototype.record.Record;

/**
 * <class comment/>
 */
public interface TemplateManager
{
    TemplateRecord load(Scope scope, String id, String recordName);

    void store(Record record);

    void delete(Record record);
}
