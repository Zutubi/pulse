package com.zutubi.pulse.master.upgrade.tasks;

import com.zutubi.tove.type.record.PathUtils;

import java.util.Arrays;
import java.util.List;

/**
 * Adds the new configAuditLoggingEnabled field to the logging configuration.
 */
public class AddConfigAuditLoggingEnabledUpgradeTask extends AbstractRecordPropertiesUpgradeTask
{
    private static final String SCOPE_GLOBAL = "global";
    private static final String PROPERTY_LOGGING = "logging";
    private static final String PROPERTY_CONFIG_AUDIT_ENABLED = "configAuditLoggingEnabled";

    public boolean haltOnFailure()
    {
        return true;
    }

    protected RecordLocator getRecordLocator()
    {
        return RecordLocators.newPathPattern(PathUtils.getPath(SCOPE_GLOBAL, PROPERTY_LOGGING));
    }

    protected List<? extends RecordUpgrader> getRecordUpgraders()
    {
        return Arrays.asList(RecordUpgraders.newAddProperty(PROPERTY_CONFIG_AUDIT_ENABLED, "false"));
    }
}
