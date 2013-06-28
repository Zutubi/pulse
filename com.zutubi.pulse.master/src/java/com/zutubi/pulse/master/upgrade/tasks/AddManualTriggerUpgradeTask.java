package com.zutubi.pulse.master.upgrade.tasks;

import com.google.common.base.Function;
import com.zutubi.pulse.master.util.monitor.TaskException;
import com.zutubi.tove.type.record.*;

/**
 * Adds a manual trigger to the global project template, and sets the prompt flag based on the
 * existing build option.
 */
public class AddManualTriggerUpgradeTask extends AbstractUpgradeTask
{
    private static final String SCOPE_PROJECTS = "projects";
    private static final String PROPERTY_TRIGGERS = "triggers";
    private static final String TRIGGER_MANUAL = "trigger build";

    private RecordManager recordManager;

    public boolean haltOnFailure()
    {
        return true;
    }

    public void execute() throws TaskException
    {
        TemplatedScopeDetails details = new TemplatedScopeDetails(SCOPE_PROJECTS, recordManager);
        details.getHierarchy().forEach(new Function<ScopeHierarchy.Node, Boolean>()
        {
            public Boolean apply(ScopeHierarchy.Node node)
            {
                String path = PathUtils.getPath(SCOPE_PROJECTS, node.getId(), PROPERTY_TRIGGERS, TRIGGER_MANUAL);
                if (recordManager.containsRecord(path))
                {
                    return true;
                }

                MutableRecord triggerRecord = new MutableRecordImpl();
                triggerRecord.setSymbolicName("zutubi.manualTriggerConfig");
                triggerRecord.put("properties", new MutableRecordImpl());
                triggerRecord.put("conditions", new MutableRecordImpl());
                if (node.getParent() == null)
                {
                    triggerRecord.put("name", TRIGGER_MANUAL);
                    triggerRecord.put("rebuildUpstreamDependencies", "false");
                }

                String prompt = getPromptValue(node);
                if (prompt != null)
                {
                    triggerRecord.put("prompt", prompt);
                }

                recordManager.insert(path, triggerRecord);
                return true;
            }
        });
    }

    private String getPromptValue(ScopeHierarchy.Node node)
    {
        Record optionsRecord = recordManager.select(PathUtils.getPath(SCOPE_PROJECTS, node.getId(), "options"));
        return optionsRecord == null ? null : (String) optionsRecord.get("prompt");
    }

    public void setRecordManager(RecordManager recordManager)
    {
        this.recordManager = recordManager;
    }
}
