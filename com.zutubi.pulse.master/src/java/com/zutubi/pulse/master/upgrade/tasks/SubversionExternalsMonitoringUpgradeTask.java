package com.zutubi.pulse.master.upgrade.tasks;

import com.zutubi.tove.type.record.PathUtils;
import static com.zutubi.tove.type.record.PathUtils.WILDCARD_ANY_ELEMENT;
import com.zutubi.tove.type.record.RecordManager;

import static java.util.Arrays.asList;
import java.util.List;

/**
 * Adds the new externalsMonitoring field to Subversion configs.
 */
public class SubversionExternalsMonitoringUpgradeTask extends AbstractRecordPropertiesUpgradeTask
{
    private static final String SCOPE_PROJECTS = "projects";
    private static final String PROPERTY_SCM = "scm";

    private static final String TYPE_SUBVERSION = "zutubi.subversionConfig";

    private static final String PROPERTY_MONITORING = "externalsMonitoring";

    public boolean haltOnFailure()
    {
        return true;
    }

    protected RecordLocator getRecordLocator()
    {
        return RecordLocators.newTypeFilter(
                RecordLocators.newPathPattern(PathUtils.getPath(SCOPE_PROJECTS, WILDCARD_ANY_ELEMENT, PROPERTY_SCM)),
                TYPE_SUBVERSION
        );
    }

    protected List<RecordUpgrader> getRecordUpgraders()
    {
        return asList(RecordUpgraders.newAddProperty(PROPERTY_MONITORING, "MONITOR_SELECTED"));
    }
}