package com.zutubi.tove.config.health;

import com.zutubi.tove.type.record.MutableRecord;
import com.zutubi.tove.type.record.Record;
import com.zutubi.tove.type.record.RecordManager;

/**
 * Indicates a template child record has a simple value where its template parent has a record.
 */
public class SimpleOverrideOfComplexProblem extends MismatchedTemplateStructureProblem
{
    public SimpleOverrideOfComplexProblem(String path, String message, String key, String templateParentPath)
    {
        super(path, message, key, templateParentPath);
    }

    @Override
    public void solve(RecordManager recordManager)
    {
        if (parentStillHasRecord(recordManager))
        {
            Record record = recordManager.select(getPath());
            if (record.containsKey(key))
            {
                MutableRecord mutableRecord = record.copy(false, true);
                mutableRecord.remove(key);
                recordManager.update(getPath(), mutableRecord);
            }
        }

        // Now let the super implementation fill in skeletons.
        super.solve(recordManager);
    }
}
