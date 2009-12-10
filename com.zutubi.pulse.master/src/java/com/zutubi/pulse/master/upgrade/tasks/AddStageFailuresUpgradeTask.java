package com.zutubi.pulse.master.upgrade.tasks;

import com.zutubi.tove.type.record.PathUtils;

import java.util.Arrays;
import java.util.List;

/**
 * Adds the new stageFailureLimit field to project build option configuration.
 */
public class AddStageFailuresUpgradeTask extends AbstractRecordPropertiesUpgradeTask
{
    private static final String SCOPE_PROJECTS = "projects";
    private static final String PROPERTY_OPTIONS = "options";
    private static final String PROPERTY_LIMIT = "stageFailureLimit";

    public boolean haltOnFailure()
    {
        return true;
    }

    protected RecordLocator getRecordLocator()
    {
        return RecordLocators.newPathPattern(PathUtils.getPath(SCOPE_PROJECTS, PathUtils.WILDCARD_ANY_ELEMENT, PROPERTY_OPTIONS));
    }

    protected List<RecordUpgrader> getRecordUpgraders()
    {
        return Arrays.asList(RecordUpgraders.newAddProperty(PROPERTY_LIMIT, "0"));
    }
}