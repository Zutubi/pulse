package com.zutubi.pulse.master.upgrade.tasks;

import com.zutubi.tove.type.record.MutableRecord;

/**
 * Deletes an existing meta property from a record.
 */
class DeleteMetaPropertyRecordUpgrader implements RecordUpgrader
{
    private String name;

    /**
     * @param name the name of the meta property to delete
     */
    public DeleteMetaPropertyRecordUpgrader(String name)
    {
        this.name = name;
    }

    public void upgrade(String path, MutableRecord record)
    {
        record.removeMeta(name);
    }
}
