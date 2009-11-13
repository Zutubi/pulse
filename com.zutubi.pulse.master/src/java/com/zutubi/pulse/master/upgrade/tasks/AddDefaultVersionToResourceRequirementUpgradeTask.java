package com.zutubi.pulse.master.upgrade.tasks;

import com.zutubi.pulse.master.util.monitor.TaskException;
import com.zutubi.tove.type.record.MutableRecord;
import com.zutubi.tove.type.record.PathUtils;
import com.zutubi.tove.type.record.Record;
import com.zutubi.tove.type.record.RecordManager;
import com.zutubi.util.StringUtils;
import com.zutubi.util.logging.Logger;

import java.util.Map;

/**
 *
 *
 */
public class AddDefaultVersionToResourceRequirementUpgradeTask extends AbstractUpgradeTask
{
    private static final Logger LOG = Logger.getLogger(AddDefaultVersionToResourceRequirementUpgradeTask.class);

    private RecordManager recordManager;

    public boolean haltOnFailure()
    {
        return false;
    }

    public void execute() throws TaskException
    {
        Map<String,Record> requirementsRecords = recordManager.selectAll(PathUtils.getPath("projects/*/requirements/*"));
        for(Map.Entry<String, Record> entry: requirementsRecords.entrySet())
        {
            Record record = entry.getValue();

            // validate that we are dealing with the expected entries.
            String symbolicName = record.getSymbolicName();
            if (!StringUtils.stringSet(symbolicName) || !symbolicName.equals("zutubi.resourceRequirementConfig"))
            {
                LOG.warning("Found unexpected record. " + symbolicName);
                continue;
            }

            if (record.containsKey("defaultVersion"))
            {
                continue;
            }

            MutableRecord copy = record.copy(true, true);

            String version = (String) record.get("version");
            if (StringUtils.stringSet(version))
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
