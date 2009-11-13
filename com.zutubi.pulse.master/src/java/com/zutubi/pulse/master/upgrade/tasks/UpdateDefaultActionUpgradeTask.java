package com.zutubi.pulse.master.upgrade.tasks;

import com.zutubi.pulse.master.util.monitor.TaskException;
import com.zutubi.tove.type.record.MutableRecord;
import com.zutubi.tove.type.record.PathUtils;
import com.zutubi.tove.type.record.Record;
import com.zutubi.tove.type.record.RecordManager;

import java.util.Map;

/**
 * Changes a user's default action to 'browse' when it is currently
 * 'projects' (CIB-1582).
 */
public class UpdateDefaultActionUpgradeTask extends AbstractUpgradeTask
{
    private RecordManager recordManager;

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
            String currentDefault = (String) record.get("defaultAction");
            if("projects".equals(currentDefault))
            {
                MutableRecord mutable = record.copy(false, true);
                mutable.put("defaultAction", "browse");
                recordManager.update(preferencesEntry.getKey(), mutable);
            }
        }
    }

    public void setRecordManager(RecordManager recordManager)
    {
        this.recordManager = recordManager;
    }
}
