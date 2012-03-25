package com.zutubi.pulse.master.upgrade.tasks;

import com.zutubi.tove.type.record.PathUtils;

import java.util.Arrays;
import java.util.List;

/**
 * Adds a new option to directory artifact configuration to control capturing as a zip.
 */
public class AddCaptureAsZipUpgradeTask extends AbstractRecordPropertiesUpgradeTask
{
    protected RecordLocator getRecordLocator()
    {
        return RecordLocators.newTypeFilter(
                RecordLocators.newPathPattern(PathUtils.getPath("projects/*/type/recipes/*/commands/*/artifacts/*")),
                "zutubi.directoryArtifactConfig"
        );
    }

    protected List<RecordUpgrader> getRecordUpgraders()
    {
        return Arrays.asList(RecordUpgraders.newAddProperty("captureAsZip", "false"));
    }

    public boolean haltOnFailure()
    {
        return true;
    }
}
