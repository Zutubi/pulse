package com.zutubi.pulse.master.upgrade.tasks;

import com.zutubi.pulse.master.util.monitor.TaskException;
import com.zutubi.tove.type.record.MutableRecord;
import com.zutubi.tove.type.record.MutableRecordImpl;
import com.zutubi.tove.type.record.PathUtils;
import com.zutubi.tove.type.record.RecordManager;
import com.zutubi.util.UnaryFunction;

/**
 * Adds the new project bootstrap configuration.
 */
public class AddBootstrapOptionsUpgradeTask extends AbstractUpgradeTask
{
    private static final String SCOPE_PROJECTS = "projects";

    private static final String TYPE_BOOTSTRAP = "zutubi.bootstrapConfig";

    private RecordManager recordManager;

    public boolean haltOnFailure()
    {
        return true;
    }

    public void execute() throws TaskException
    {
        TemplatedScopeDetails details = new TemplatedScopeDetails(SCOPE_PROJECTS, recordManager);
        details.getHierarchy().forEach(new UnaryFunction<ScopeHierarchy.Node, Boolean>()
        {
            public Boolean process(ScopeHierarchy.Node node)
            {
                String path = PathUtils.getPath(SCOPE_PROJECTS, node.getId(), "bootstrap");
                if (recordManager.containsRecord(path))
                {
                    return true;
                }
                
                MutableRecord bootstrapRecord = new MutableRecordImpl();
                bootstrapRecord.setSymbolicName(TYPE_BOOTSTRAP);
                if (node.getParent() == null)
                {
                    bootstrapRecord.put("checkoutType", "CLEAN_CHECKOUT");
                    bootstrapRecord.put("buildType", "CLEAN_BUILD");
                    bootstrapRecord.put("persistentDirPattern", "$(agent.data.dir)/work/$(project.handle)/$(stage.handle)");
                    bootstrapRecord.put("tempDirPattern", "$(agent.data.dir)/recipes/$(recipe.id)/base");
                }

                recordManager.insert(path, bootstrapRecord);
                return true;
            }
        });
    }

    public void setRecordManager(RecordManager recordManager)
    {
        this.recordManager = recordManager;
    }
}
