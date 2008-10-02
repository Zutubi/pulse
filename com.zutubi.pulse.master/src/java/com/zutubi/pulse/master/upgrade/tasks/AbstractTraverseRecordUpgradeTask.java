package com.zutubi.pulse.master.upgrade.tasks;

import com.zutubi.tove.type.record.MutableRecord;

/**
 *
 *
 */
public abstract class AbstractTraverseRecordUpgradeTask extends AbstractUpgradeTask
{
    protected void traverse(MutableRecord record)
    {
        doUpgrade(record);

        // traverse nested children in a depth first traversal.
        for (String key : record.keySet())
        {
            Object value = record.get(key);
            if (value instanceof MutableRecord)
            {
                MutableRecord nestedRecord = (MutableRecord) value;
                traverse(nestedRecord);
            }
        }
    }

    public abstract void doUpgrade(MutableRecord record);
}
