package com.zutubi.pulse.master.upgrade.tasks;

import com.zutubi.tove.type.record.MutableRecord;

/**
 * Deletes an existing simple property from a record.
 */
class DeletePropertyRecordUpgrader implements RecordUpgrader
{
    private String name;

    /**
     * @param name the name of the property to delete, must be a simple
     *        property (not a nested record)
     */
    public DeletePropertyRecordUpgrader(String name)
    {
        this.name = name;
    }

    public void upgrade(String path, MutableRecord record)
    {
        Object value = record.remove(name);
        if (value != null && !RecordUpgradeUtils.isSimpleValue(value))
        {
            throw new IllegalArgumentException("Attempt to delete a non-simple value (existing value of property '" + name + "' has type '" + value.getClass() + "')");
        }
    }
}
