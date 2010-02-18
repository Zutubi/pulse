package com.zutubi.pulse.master.upgrade.tasks;

import com.zutubi.tove.type.record.PathUtils;

import java.util.Arrays;
import java.util.List;

/**
 * Adds a new option to PerforceConfiguration to allow the inactivity timeout
 * to be tweaked.
 */
public class AddPerforceInactivityTimeoutUpgradeTask extends AbstractRecordPropertiesUpgradeTask
{
    private static final String SCOPE_PROJECTS = "projects";
    private static final String TYPE_PERFORCE = "zutubi.perforceConfig";
    private static final String PROPERTY_SCM = "scm";
    private static final String PROPERTY_TIMEOUT = "inactivityTimeout";

    public boolean haltOnFailure()
    {
        return true;
    }

    protected RecordLocator getRecordLocator()
    {
        return RecordLocators.newTypeFilter(
                RecordLocators.newPathPattern(PathUtils.getPath(SCOPE_PROJECTS, PathUtils.WILDCARD_ANY_ELEMENT, PROPERTY_SCM)),
                TYPE_PERFORCE
        );
    }

    protected List<RecordUpgrader> getRecordUpgraders()
    {
        // Set to zero for backwards compatibility.
        return Arrays.asList(RecordUpgraders.newAddProperty(PROPERTY_TIMEOUT, "0"));
    }
}