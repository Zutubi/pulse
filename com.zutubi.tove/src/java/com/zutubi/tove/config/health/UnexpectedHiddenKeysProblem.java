package com.zutubi.tove.config.health;

import com.zutubi.tove.type.record.MutableRecord;
import com.zutubi.tove.type.record.Record;
import com.zutubi.tove.type.record.RecordManager;
import com.zutubi.tove.type.record.TemplateRecord;

/**
 * This problem indicates that hidden keys have been found in a record with no
 * template parent.
 */
public class UnexpectedHiddenKeysProblem extends HealthProblemSupport
{
    /**
     * Creates a new problem indicating unexpected hidden keys at the given
     * path.
     * 
     * @param path    path of the record with the hidden keys
     * @param message description of the problem
     */
    public UnexpectedHiddenKeysProblem(String path, String message)
    {
        super(path, message);
    }

    public void solve(RecordManager recordManager)
    {
        Record record = recordManager.select(getPath());
        if (record != null && record.containsMetaKey(TemplateRecord.HIDDEN_KEY))
        {
            MutableRecord mutableRecord = record.copy(false, true);
            mutableRecord.removeMeta(TemplateRecord.HIDDEN_KEY);
            recordManager.update(getPath(), mutableRecord);
        }
    }
}
