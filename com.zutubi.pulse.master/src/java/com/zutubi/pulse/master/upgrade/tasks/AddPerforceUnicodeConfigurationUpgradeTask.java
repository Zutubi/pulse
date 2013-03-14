package com.zutubi.pulse.master.upgrade.tasks;

import com.zutubi.tove.type.record.PathUtils;

import java.util.Arrays;
import java.util.List;

/**
 * Upgrade task to add two new properties to the perforce configuration,
 * the unicodeServer flag and the charset field.
 */
public class AddPerforceUnicodeConfigurationUpgradeTask extends AbstractRecordPropertiesUpgradeTask
{
    private static final String PROPERTY_UNICODE_SERVER = "unicodeServer";
    private static final String PROPERTY_CHARSET = "charset";
    private static final String SCOPE_PROJECTS = "projects";
    private static final String PROPERTY_SCM = "scm";
    private static final String TYPE_PERFORCE = "zutubi.perforceConfig";

    public boolean haltOnFailure()
    {
        return true;
    }

    protected RecordLocator getRecordLocator()
    {
        RecordLocator locator = RecordLocators.newPathPattern(PathUtils.getPath(SCOPE_PROJECTS, PathUtils.WILDCARD_ANY_ELEMENT, PROPERTY_SCM));
        return RecordLocators.newTypeFilter(locator, TYPE_PERFORCE);
    }

    protected List<? extends RecordUpgrader> getRecordUpgraders()
    {
        return Arrays.asList(
                RecordUpgraders.newAddProperty(PROPERTY_UNICODE_SERVER, Boolean.toString(false)),
                RecordUpgraders.newAddProperty(PROPERTY_CHARSET, "none")
        );
    }

}
