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
public class ScmCommitterMappingsUpgradeTask extends AbstractUpgradeTask
{
    private static final String SCOPE_PROJECTS = "projects";
    private static final String PROPERTY_SCM = "scm";

    private static final String PROPERTY_MAPPINGS = "committerMappings";

    private RecordManager recordManager;

    public boolean haltOnFailure()
    {
        return true;
    }

    public void execute() throws TaskException
    {
        Map<String,Record> scmRecords = recordManager.selectAll(getPath(SCOPE_PROJECTS, WILDCARD_ANY_ELEMENT, PROPERTY_SCM));
        for (Map.Entry<String, Record> pathRecord: scmRecords.entrySet())
        {
            recordManager.insert(getPath(pathRecord.getKey(), PROPERTY_MAPPINGS), new MutableRecordImpl());
        }
    }

    public void setRecordManager(RecordManager recordManager)
    {
        this.recordManager = recordManager;
    }
}