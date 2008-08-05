package com.zutubi.pulse.upgrade.tasks;

import com.zutubi.pulse.monitor.TaskException;
import com.zutubi.tove.type.record.MutableRecord;
import com.zutubi.tove.type.record.PathUtils;
import com.zutubi.tove.type.record.Record;
import com.zutubi.tove.type.record.RecordManager;
import com.zutubi.util.TextUtils;

import java.util.Map;

/**
 *
 *
 */
public class AddDefaultVersionToResourceRequirementUpgradeTask extends AbstractUpgradeTask
{
    private RecordManager recordManager;

    public String getName()
    {
        return "Resource Requirement Configuration";
    }

    public String getDescription()
    {
        return "Add the new 'default version' field to the resource requirement configuration.";
    }

    public boolean haltOnFailure()
    {
        return false;
    }

    public void execute() throws TaskException
    {
        Map<String,Record> requirementsRecords = recordManager.selectAll(PathUtils.getPath("projects/*/requirements"));
        for(Map.Entry<String, Record> entry: requirementsRecords.entrySet())
        {
            Record record = entry.getValue();
            if (record.containsKey("defaultVersion"))
            {
                continue;
            }

            MutableRecord copy = record.copy(true);

            String version = (String) record.get("version");
            if (TextUtils.stringSet(version))
            {
                copy.put("defaultVersion", "false");
            }
            else
            {
                copy.put("defaultVersion", "true");
            }

            recordManager.update(entry.getKey(), copy);
        }

    }

    public void setRecordManager(RecordManager recordManager)
    {
        this.recordManager = recordManager;
    }
}
