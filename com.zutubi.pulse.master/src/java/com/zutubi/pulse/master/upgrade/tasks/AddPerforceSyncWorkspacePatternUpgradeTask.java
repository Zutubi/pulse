package com.zutubi.pulse.master.upgrade.tasks;

import com.zutubi.tove.type.record.PathUtils;

import java.util.Arrays;
import java.util.List;

/**
 * Adds the syncWorkspacePattern field to the perforce configuration.
 */
public class AddPerforceSyncWorkspacePatternUpgradeTask extends AbstractRecordPropertiesUpgradeTask
{
    private static final String PROPERTY = "syncWorkspacePattern";
    private static final String DEFAULT_VALUE = System.getProperty("pulse.p4.client.prefix", "pulse-") + "$(project.handle)-$(agent.handle)";

    protected RecordLocator getRecordLocator()
    {
        return RecordLocators.newTypeFilter(RecordLocators.newPathPattern(PathUtils.getPath("projects", PathUtils.WILDCARD_ANY_ELEMENT, "scm")), "zutubi.perforceConfig");
    }

    protected List<RecordUpgrader> getRecordUpgraders()
    {
        return Arrays.asList(RecordUpgraders.newAddProperty(PROPERTY, DEFAULT_VALUE));
    }

    public boolean haltOnFailure()
    {
        return true;
    }
}
