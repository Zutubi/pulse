package com.zutubi.pulse.master.upgrade.tasks;

import com.zutubi.tove.type.record.MutableRecord;

/**
 * Interface representing a single upgrade on a single record.  Multiple such
 * upgrades may be composed and applied to multiple records in a single upgrade
 * task.  To obtain instances of upgraders, see the {@link RecordUpgraders}
 * class.
 */
public interface RecordUpgrader
{
    /**
     * Perform a simple upgrade on the given record.  Note that updates may
     * only be made to simple values directly within the record.  Changes to
     * nested records are not supported and will have an affect between none
     * and disastrous.
     *
     * @param path   path at which the record is stored
     * @param record the record to upgrade
     */
    void upgrade(String path, MutableRecord record);
}
