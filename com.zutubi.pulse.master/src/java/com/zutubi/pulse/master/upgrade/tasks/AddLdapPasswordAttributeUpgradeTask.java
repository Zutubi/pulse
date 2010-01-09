package com.zutubi.pulse.master.upgrade.tasks;

import com.zutubi.tove.type.record.PathUtils;

import java.util.Arrays;
import java.util.List;

/**
 * Adds the new passwordAttribute field to the LDAP configuration.
 */
public class AddLdapPasswordAttributeUpgradeTask extends AbstractRecordPropertiesUpgradeTask
{
    private static final String SCOPE_GLOBAL = "global";
    private static final String PROPERTY_LDAP = "ldap";
    private static final String PROPERTY_PASSWORD_ATTRIBUTE = "passwordAttribute";

    public boolean haltOnFailure()
    {
        return true;
    }

    protected RecordLocator getRecordLocator()
    {
        return RecordLocators.newPathPattern(PathUtils.getPath(SCOPE_GLOBAL, PROPERTY_LDAP));
    }

    protected List<RecordUpgrader> getRecordUpgraders()
    {
        return Arrays.asList(RecordUpgraders.newAddProperty(PROPERTY_PASSWORD_ATTRIBUTE, ""));
    }
}