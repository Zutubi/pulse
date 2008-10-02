package com.zutubi.pulse.master.upgrade.tasks;

import com.zutubi.pulse.master.monitor.TaskException;
import com.zutubi.tove.type.record.*;

import java.util.Map;

/**
 * Adds records for the new BrowseViewSettingsConfiguration to users.
 */
public class AddBrowseViewConfigurationUpgradeTask extends AbstractUpgradeTask
{
    private RecordManager recordManager;

    public String getName()
    {
        return "Browse View Configuration";
    }

    public String getDescription()
    {
        return "Add a new configuration record to each user for browse view settings.";
    }

    public boolean haltOnFailure()
    {
        return true;
    }

    public void execute() throws TaskException
    {
        Map<String,Record> preferencesRecords = recordManager.selectAll(PathUtils.getPath("users/*/preferences"));
        for(Map.Entry<String, Record> preferencesEntry: preferencesRecords.entrySet())
        {
            Record record = preferencesEntry.getValue();
            if (record.containsKey("browseView"))
            {
                continue;
            }

            MutableRecord newRecord = new MutableRecordImpl();
            newRecord.setSymbolicName("zutubi.browseViewConfig");
            newRecord.setPermanent(true);
            newRecord.put("groupsShown", Boolean.TRUE.toString());
            newRecord.put("hierarchyShown", Boolean.TRUE.toString());
            newRecord.put("hiddenHierarchyLevels", Integer.toString(1));
            newRecord.put("buildsPerProject", Integer.toString(1));
            newRecord.put("columns", new String[]{"when", "elapsed", "reason", "tests"});

            recordManager.insert(preferencesEntry.getKey() + "/browseView", newRecord);
        }
    }

    public void setRecordManager(RecordManager recordManager)
    {
        this.recordManager = recordManager;
    }
}
