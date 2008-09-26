package com.zutubi.pulse.upgrade.tasks;

import com.zutubi.tove.type.record.PathUtils;
import com.zutubi.pulse.core.config.Configuration;

import java.util.List;
import java.util.Arrays;

/**
 * Adds the permanent flag to backup configuration if it is not already
 * present.
 */
public class EnsureBackupConfigPermanentUpgradeTask extends AbstractRecordPropertiesUpgradeTask
{
    private static final String SCOPE_SETTINGS = "settings";

    public String getName()
    {
        return "Ensure Backup Config Permanent";
    }

    public String getDescription()
    {
        return "Marks the backup configuration as permanent (it should not be deletable).";
    }

    public boolean haltOnFailure()
    {
        return true;
    }

    protected String getScope()
    {
        return SCOPE_SETTINGS;
    }

    protected RecordLocator getRecordLocator()
    {
        return RecordLocators.newPath(PathUtils.getPath(SCOPE_SETTINGS, "backup"));
    }

    protected List<RecordUpgrader> getRecordUpgraders()
    {
        return Arrays.asList(RecordUpgraders.newAddMetaProperty(Configuration.PERMANENT_KEY, Boolean.toString(true)));
    }
}
