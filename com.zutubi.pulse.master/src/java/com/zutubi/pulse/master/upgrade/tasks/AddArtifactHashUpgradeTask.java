package com.zutubi.pulse.master.upgrade.tasks;

import java.util.List;

import static com.zutubi.pulse.master.upgrade.tasks.RecordLocators.newPathPattern;
import static com.zutubi.pulse.master.upgrade.tasks.RecordLocators.newTypeFilter;
import static com.zutubi.pulse.master.upgrade.tasks.RecordUpgraders.newAddProperty;
import static com.zutubi.tove.type.record.PathUtils.WILDCARD_ANY_ELEMENT;
import static com.zutubi.tove.type.record.PathUtils.getPath;
import static java.util.Arrays.asList;

/**
 * Adds the new hashing options to file system artifacts.
 */
public class AddArtifactHashUpgradeTask extends AbstractRecordPropertiesUpgradeTask
{
    public boolean haltOnFailure()
    {
        return true;
    }

    protected RecordLocator getRecordLocator()
    {
        String allArtifactsPattern = getPath("projects", WILDCARD_ANY_ELEMENT, "type", "recipes", WILDCARD_ANY_ELEMENT, "commands", WILDCARD_ANY_ELEMENT, "artifacts", WILDCARD_ANY_ELEMENT);
        return newTypeFilter(newPathPattern(allArtifactsPattern),
                             "zutubi.fileArtifactConfig", "zutubi.directoryArtifactConfig");
    }

    protected List<? extends RecordUpgrader> getRecordUpgraders()
    {
        return asList(
                newAddProperty("calculateHash", Boolean.toString(false)),
                newAddProperty("hashAlgorithm", "MD5")
        );
    }
}