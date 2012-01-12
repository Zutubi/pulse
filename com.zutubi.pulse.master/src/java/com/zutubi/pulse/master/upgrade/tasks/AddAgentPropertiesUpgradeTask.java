package com.zutubi.pulse.master.upgrade.tasks;

import com.zutubi.tove.type.record.MutableRecordImpl;
import com.zutubi.tove.type.record.PathUtils;
import com.zutubi.tove.type.record.Record;
import com.zutubi.tove.type.record.RecordManager;

import java.util.Map;

import static com.zutubi.tove.type.record.PathUtils.WILDCARD_ANY_ELEMENT;
import static com.zutubi.tove.type.record.PathUtils.getPath;

/**
 * Upgrade task to add the properties collection to instances of AgentConfiguration.
 */
public class AddAgentPropertiesUpgradeTask extends AbstractUpgradeTask
{
    private static final String SCOPE = "agents";
    private static final String PROPERTY = "properties";

    private RecordManager recordManager;

    public boolean haltOnFailure()
    {
        return true;
    }

    protected RecordLocator getRecordLocator()
    {
        return RecordLocators.newPathPattern(PathUtils.getPath(SCOPE, PathUtils.WILDCARD_ANY_ELEMENT));
    }

    public void execute()
    {
        Map<String,Record> agentRecords = recordManager.selectAll(getPath(SCOPE, WILDCARD_ANY_ELEMENT));
        for (Map.Entry<String, Record> pathRecord: agentRecords.entrySet())
        {
            recordManager.insert(getPath(pathRecord.getKey(), PROPERTY), new MutableRecordImpl());
        }
    }

    public void setRecordManager(RecordManager recordManager)
    {
        this.recordManager = recordManager;
    }
}
