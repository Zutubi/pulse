package com.zutubi.pulse.master.upgrade.tasks;

import com.zutubi.tove.config.api.Configuration;
import com.zutubi.tove.type.record.PathUtils;

import java.util.Arrays;
import java.util.List;

/**
 * Adds the permanent flag to all global configuration if it is not already
 * present.
 */
public class EnsureGlobalConfigPermanentUpgradeTask extends AbstractRecordPropertiesUpgradeTask
{
    private static final String SCOPE_SETTINGS = "settings";

    public boolean haltOnFailure()
    {
        return false;
    }

    protected RecordLocator getRecordLocator()
    {
        return RecordLocators.newPathPattern(PathUtils.getPath(SCOPE_SETTINGS, PathUtils.WILDCARD_ANY_ELEMENT));
    }

    protected List<? extends RecordUpgrader> getRecordUpgraders()
    {
        return Arrays.asList(RecordUpgraders.newAddMetaProperty(Configuration.PERMANENT_KEY, Boolean.toString(true)));
    }
}