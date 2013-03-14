package com.zutubi.pulse.master.upgrade.tasks;

import com.zutubi.tove.config.api.Configuration;
import com.zutubi.tove.type.record.PathUtils;

import java.util.Arrays;
import java.util.List;

/**
 * Adds the permanent flag to backup configuration if it is not already
 * present.
 */
public class EnsureBackupConfigPermanentUpgradeTask extends AbstractRecordPropertiesUpgradeTask
{
    private static final String SCOPE_SETTINGS = "settings";

    public boolean haltOnFailure()
    {
        return true;
    }

    protected RecordLocator getRecordLocator()
    {
        return RecordLocators.newPath(PathUtils.getPath(SCOPE_SETTINGS, "backup"));
    }

    protected List<? extends RecordUpgrader> getRecordUpgraders()
    {
        return Arrays.asList(RecordUpgraders.newAddMetaProperty(Configuration.PERMANENT_KEY, Boolean.toString(true)));
    }
}
