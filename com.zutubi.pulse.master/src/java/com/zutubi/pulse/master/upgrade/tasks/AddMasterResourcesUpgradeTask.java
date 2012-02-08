package com.zutubi.pulse.master.upgrade.tasks;

import com.zutubi.tove.type.record.MutableRecordImpl;
import com.zutubi.tove.type.record.RecordManager;

/**
 * Adds a new empty collection of resources to the global configuration on the master.
 */
public class AddMasterResourcesUpgradeTask extends AbstractUpgradeTask
{
    private RecordManager recordManager;

    public boolean haltOnFailure()
    {
        return true;
    }

    public void execute()
    {
        recordManager.insert("settings/resources", new MutableRecordImpl());
    }

    public void setRecordManager(RecordManager recordManager)
    {
        this.recordManager = recordManager;
    }
}
