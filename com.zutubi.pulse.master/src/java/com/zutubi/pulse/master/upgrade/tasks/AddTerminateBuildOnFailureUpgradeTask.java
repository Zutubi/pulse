package com.zutubi.pulse.master.upgrade.tasks;

import com.zutubi.tove.type.record.PathUtils;

import java.util.Arrays;
import java.util.List;

/**
 * Adds the new terminateBuildOnFailure field to project build stage configuration.
 */
public class AddTerminateBuildOnFailureUpgradeTask extends AbstractRecordPropertiesUpgradeTask
{
    private static final String SCOPE_PROJECTS = "projects";
    private static final String PROPERTY_STAGES = "stages";
    private static final String PROPERTY_TERMINATE = "terminateBuildOnFailure";

    public boolean haltOnFailure()
    {
        return true;
    }

    protected RecordLocator getRecordLocator()
    {
        return RecordLocators.newPathPattern(PathUtils.getPath(SCOPE_PROJECTS, PathUtils.WILDCARD_ANY_ELEMENT, PROPERTY_STAGES, PathUtils.WILDCARD_ANY_ELEMENT));
    }

    protected List<? extends RecordUpgrader> getRecordUpgraders()
    {
        return Arrays.asList(RecordUpgraders.newAddProperty(PROPERTY_TERMINATE, "false"));
    }
}