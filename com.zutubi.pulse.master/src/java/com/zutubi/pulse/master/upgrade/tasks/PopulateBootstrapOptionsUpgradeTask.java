package com.zutubi.pulse.master.upgrade.tasks;

import com.zutubi.pulse.master.util.monitor.TaskException;
import com.zutubi.tove.type.record.MutableRecord;
import com.zutubi.tove.type.record.Record;
import com.zutubi.tove.type.record.RecordManager;
import com.zutubi.util.UnaryFunction;

/**
 * Populates the new bootstrap options based on existing checkout schemes and
 * custom persistent work directories.
 */
public class PopulateBootstrapOptionsUpgradeTask extends AbstractUpgradeTask
{
    private static final String CHECKOUT_SCHEME_CLEAN_CHECKOUT = "CLEAN_CHECKOUT";
    private static final String CHECKOUT_SCHEME_CLEAN_UPDATE = "CLEAN_UPDATE";
    private static final String CHECKOUT_TYPE_CLEAN = "CLEAN_CHECKOUT";
    private static final String CHECKOUT_TYPE_INCREMENTAL = "INCREMENTAL_CHECKOUT";
    private static final String BUILD_TYPE_CLEAN = "CLEAN_BUILD";
    private static final String BUILD_TYPE_INCREMENTAL = "INCREMENTAL_BUILD";

    private RecordManager recordManager;

    public boolean haltOnFailure()
    {
        return true;
    }

    public void execute() throws TaskException
    {
        PersistentScopes persistentScopes = new PersistentScopes(recordManager);
        TemplatedScopeDetails projectsScope = (TemplatedScopeDetails) persistentScopes.getScopeDetails("projects");
        projectsScope.getHierarchy().forEach(new UnaryFunction<ScopeHierarchy.Node, Boolean>()
        {
            public Boolean process(ScopeHierarchy.Node node)
            {
                String project = node.getId();
                String projectPath = "projects/" + project;
                Record projectRecord = recordManager.select(projectPath);

                String persistentDir = null;
                String checkoutType = null;
                String buildType = null;

                Record options = (Record) projectRecord.get("options");
                if (options != null)
                {
                    persistentDir = (String) options.get("persistentWorkDir");
                }

                Record scm = (Record) projectRecord.get("scm");
                if (scm != null)
                {
                    String checkoutScheme = (String) scm.get("checkoutScheme");
                    if (checkoutScheme != null)
                    {
                        String parentCheckoutScheme = null;
                        ScopeHierarchy.Node parentNode = node.getParent();
                        if (parentNode != null)
                        {
                            String parent = parentNode.getId();
                            Record parentScmRecord = recordManager.select("projects/" + parent + "/scm");
                            parentCheckoutScheme = parentScmRecord == null ? null : (String) parentScmRecord.get("checkoutScheme");
                        }

                        if (parentCheckoutScheme == null)
                        {
                            parentCheckoutScheme = CHECKOUT_SCHEME_CLEAN_CHECKOUT;
                        }

                        String parentCheckoutType;
                        String parentBuildType;
                        if (parentCheckoutScheme.equals(CHECKOUT_SCHEME_CLEAN_CHECKOUT))
                        {
                            parentCheckoutType = CHECKOUT_TYPE_CLEAN;
                            parentBuildType = BUILD_TYPE_CLEAN;
                        }
                        else if (parentCheckoutScheme.equals(CHECKOUT_SCHEME_CLEAN_UPDATE))
                        {
                            parentCheckoutType = CHECKOUT_TYPE_INCREMENTAL;
                            parentBuildType = BUILD_TYPE_CLEAN;
                        }
                        else
                        {
                            parentCheckoutType = CHECKOUT_TYPE_INCREMENTAL;
                            parentBuildType = BUILD_TYPE_INCREMENTAL;
                        }

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

                        if (checkoutType.equals(parentCheckoutType))
                        {
                            // Scrub
                            checkoutType = null;
                        }

                        if (buildType.equals(parentBuildType))
                        {
                            // Scrub
                            buildType = null;
                        }
                    }
                }

                if (persistentDir != null || checkoutType != null || buildType != null)
                {
                    MutableRecord bootstrap = ((Record) projectRecord.get("bootstrap")).copy(false, true);
                    if (persistentDir != null)
                    {
                        bootstrap.put("persistentDirPattern", persistentDir);
                    }

                    if (checkoutType != null)
                    {
                        bootstrap.put("checkoutType", checkoutType);
                    }

                    if (buildType != null)
                    {
                        bootstrap.put("buildType", buildType);
                    }

                    recordManager.update(projectPath + "/bootstrap", bootstrap);
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
