package com.zutubi.pulse.master.upgrade.tasks;

import com.zutubi.tove.type.record.PathUtils;

import java.util.Arrays;
import java.util.List;

/**
 * Adds a new option to project dependencies configuration to control unzipping of archives.
 */
public class AddDependencyUnzipUpgradeTask extends AbstractRecordPropertiesUpgradeTask
{
    protected RecordLocator getRecordLocator()
    {
        return RecordLocators.newPathPattern(PathUtils.getPath("projects/*/dependencies"));
    }

    protected List<RecordUpgrader> getRecordUpgraders()
    {
        return Arrays.asList(RecordUpgraders.newAddProperty("unzipRetrievedArchives", "false"));
    }

    public boolean haltOnFailure()
    {
        return true;
    }
}
