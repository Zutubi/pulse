package com.zutubi.pulse.master.upgrade.tasks;

import java.util.Arrays;
import java.util.List;

/**
 * Add the concurrent builds field to the global project template.
 */
public class AddConcurrentBuildsFieldUpgradeTask extends AbstractRecordPropertiesUpgradeTask
{
    private static final String SCOPE_PROJECTS = "projects";
    private static final String PROPERTY_CONCURRENT_BUILDS = "concurrentBuilds";

    public boolean haltOnFailure()
    {
        return true;
    }

    protected RecordLocator getRecordLocator()
    {
        TemplatedScopeDetails details = new TemplatedScopeDetails(SCOPE_PROJECTS, recordManager);
        String globalProjectName = details.getHierarchy().getRoot().getId();

        return RecordLocators.newPathPattern(SCOPE_PROJECTS + "/"+globalProjectName+"/options");
    }

    protected List<RecordUpgrader> getRecordUpgraders()
    {
        return Arrays.asList(RecordUpgraders.newAddProperty(PROPERTY_CONCURRENT_BUILDS, "1"));
    }
}
