package com.zutubi.pulse.master.upgrade.tasks;

import com.zutubi.pulse.master.util.monitor.TaskException;
import com.zutubi.tove.type.record.MutableRecordImpl;
import static com.zutubi.tove.type.record.PathUtils.WILDCARD_ANY_ELEMENT;
import static com.zutubi.tove.type.record.PathUtils.getPath;
import com.zutubi.tove.type.record.Record;
import com.zutubi.tove.type.record.RecordManager;

import java.util.Map;

/**
 * Adds the new properties collection to triggers.
 */
public class TriggerPropertiesUpgradeTask extends AbstractUpgradeTask
{
    private static final String SCOPE_PROJECTS = "projects";
    private static final String PROPERTY_TRIGGERS = "triggers";

    private RecordManager recordManager;
    private static final String PROPERTY_PROPERTIES = "properties";

    public boolean haltOnFailure()
    {
        return true;
    }

    public void execute() throws TaskException
    {
        Map<String,Record> triggerRecords = recordManager.selectAll(getPath(SCOPE_PROJECTS, WILDCARD_ANY_ELEMENT, PROPERTY_TRIGGERS, WILDCARD_ANY_ELEMENT));
        for (Map.Entry<String, Record> pathRecord: triggerRecords.entrySet())
        {
            recordManager.insert(getPath(pathRecord.getKey(), PROPERTY_PROPERTIES), new MutableRecordImpl());
        }
    }

    public void setRecordManager(RecordManager recordManager)
    {
        this.recordManager = recordManager;
    }
}
