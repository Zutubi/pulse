package com.zutubi.pulse.master.upgrade.tasks;

import com.google.common.base.Function;
import com.zutubi.pulse.master.util.monitor.TaskException;
import com.zutubi.tove.type.record.MutableRecord;
import com.zutubi.tove.type.record.Record;
import com.zutubi.tove.type.record.RecordManager;

/**
 * Populates the new storage options for agents based on existing data directory patterns.
 */
public class PopulateAgentStorageConfigurationUpgradeTask extends AbstractUpgradeTask
{
    private static final String SCOPE = "agents";
    private static final String PROPERTY_STORAGE = "storage";
    private static final String PROPERTY_DATA_DIRECTORY = "dataDirectory";

    private RecordManager recordManager;

    public boolean haltOnFailure()
    {
        return true;
    }

    public void execute() throws TaskException
    {
        PersistentScopes persistentScopes = new PersistentScopes(recordManager);
        TemplatedScopeDetails agentsScope = (TemplatedScopeDetails) persistentScopes.getScopeDetails(SCOPE);
        agentsScope.getHierarchy().forEach(new Function<ScopeHierarchy.Node, Boolean>()
        {
            public Boolean apply(ScopeHierarchy.Node node)
            {
                String agent = node.getId();
                String agentPath = SCOPE + "/" + agent;
                Record agentRecord = recordManager.select(agentPath);

                Object dataDirectory = agentRecord.get(PROPERTY_DATA_DIRECTORY);
                if (dataDirectory != null)
                {
                    MutableRecord bootstrap = ((Record) agentRecord.get(PROPERTY_STORAGE)).copy(false, true);
                    bootstrap.put(PROPERTY_DATA_DIRECTORY, dataDirectory);
                    recordManager.update(agentPath + "/" + "storage", bootstrap);
                }

                return true;
            }
        });
    }

    public void setRecordManager(RecordManager recordManager)
    {
        this.recordManager = recordManager;
    }
}
