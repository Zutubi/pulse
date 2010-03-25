package com.zutubi.pulse.master.upgrade.tasks;

import static com.zutubi.tove.type.record.PathUtils.WILDCARD_ANY_ELEMENT;
import static com.zutubi.tove.type.record.PathUtils.getPath;

import java.util.Arrays;
import java.util.List;

/**
 * Adds the new featured flag to artifact configurations.
 */
public class AddFeaturedArtifactsUpgradeTask extends AbstractRecordPropertiesUpgradeTask
{
    public boolean haltOnFailure()
    {
        return true;
    }

    protected RecordLocator getRecordLocator()
    {
        return RecordLocators.newPathPattern(getPath("projects", WILDCARD_ANY_ELEMENT, "type", "recipes", WILDCARD_ANY_ELEMENT, "commands", WILDCARD_ANY_ELEMENT, "artifacts", WILDCARD_ANY_ELEMENT));
    }

    protected List<RecordUpgrader> getRecordUpgraders()
    {
        return Arrays.asList(RecordUpgraders.newAddProperty("featured", Boolean.toString(false)));
    }
}