package com.zutubi.pulse.master.upgrade.tasks;

import com.zutubi.pulse.master.util.monitor.TaskException;
import com.zutubi.tove.type.record.MutableRecordImpl;
import static com.zutubi.tove.type.record.PathUtils.WILDCARD_ANY_ELEMENT;
import static com.zutubi.tove.type.record.PathUtils.getPath;
import com.zutubi.tove.type.record.Record;
import com.zutubi.tove.type.record.RecordManager;

import java.util.Map;

/**
 * Adds the new committer mappings collection to email committers hook tasks.
 */
public class EmailCommittersMappingsUpgradeTask extends AbstractUpgradeTask
{
    private static final String SCOPE_PROJECTS = "projects";
    private static final String PROPERTY_HOOKS = "buildHooks";
    private static final String PROPERTY_TASK = "task";

    private static final String PROPERTY_MAPPINGS = "committerMappings";

    private static final String TYPE_EMAIL_COMMITTERS = "zutubi.emailCommittersTaskConfig";

    private RecordManager recordManager;

    public boolean haltOnFailure()
    {
        return true;
    }

    public void execute() throws TaskException
    {
        Map<String,Record> taskRecords = recordManager.selectAll(getPath(SCOPE_PROJECTS, WILDCARD_ANY_ELEMENT, PROPERTY_HOOKS, WILDCARD_ANY_ELEMENT, PROPERTY_TASK));
        for (Map.Entry<String, Record> pathRecord: taskRecords.entrySet())
        {
            if (pathRecord.getValue().getSymbolicName().equals(TYPE_EMAIL_COMMITTERS))
            {
                recordManager.insert(getPath(pathRecord.getKey(), PROPERTY_MAPPINGS), new MutableRecordImpl());
            }
        }
    }

    public void setRecordManager(RecordManager recordManager)
    {
        this.recordManager = recordManager;
    }
}