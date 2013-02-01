package com.zutubi.pulse.master.upgrade.tasks;

import com.google.common.base.Function;
import com.zutubi.pulse.master.util.monitor.TaskException;
import com.zutubi.tove.type.record.*;
import com.zutubi.util.StringUtils;

/**
 * Adds the new project bootstrap configuration.
 */
public class AddBootstrapOptionsUpgradeTask extends AbstractUpgradeTask
{
    private static final String CHECKOUT_SCHEME_CLEAN_CHECKOUT = "CLEAN_CHECKOUT";
    private static final String CHECKOUT_SCHEME_CLEAN_UPDATE = "CLEAN_UPDATE";
    private static final String CHECKOUT_TYPE_CLEAN = "CLEAN_CHECKOUT";
    private static final String CHECKOUT_TYPE_INCREMENTAL = "INCREMENTAL_CHECKOUT";
    private static final String BUILD_TYPE_CLEAN = "CLEAN_BUILD";
    private static final String BUILD_TYPE_INCREMENTAL = "INCREMENTAL_BUILD";

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
        details.getHierarchy().forEach(new Function<ScopeHierarchy.Node, Boolean>()
        {
            public Boolean apply(ScopeHierarchy.Node node)
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
                    String ownerId = node.getId();
                    Record scmRecord = recordManager.select("projects/" + ownerId + "/scm");
                    String checkoutScheme = scmRecord == null ? null : (String) scmRecord.get("checkoutScheme");
                    if (!StringUtils.stringSet(checkoutScheme))
                    {
                        checkoutScheme = CHECKOUT_SCHEME_CLEAN_CHECKOUT;
                    }

                    String checkoutType;
                    String buildType;
                    if (checkoutScheme.equals(CHECKOUT_SCHEME_CLEAN_CHECKOUT))
                    {
                        checkoutType = CHECKOUT_TYPE_CLEAN;
                        buildType = BUILD_TYPE_CLEAN;
                    }
                    else if (checkoutScheme.equals(CHECKOUT_SCHEME_CLEAN_UPDATE))
                    {
                        checkoutType = CHECKOUT_TYPE_INCREMENTAL;
                        buildType = BUILD_TYPE_CLEAN;
                    }
                    else
                    {
                        checkoutType = CHECKOUT_TYPE_INCREMENTAL;
                        buildType = BUILD_TYPE_INCREMENTAL;
                    }

                    bootstrapRecord.put("checkoutType", checkoutType);
                    bootstrapRecord.put("buildType", buildType);
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
