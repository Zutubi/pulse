package com.zutubi.pulse.master.upgrade.tasks;

import com.zutubi.tove.type.record.PathUtils;

import java.util.List;
import java.util.Arrays;

/**
 * Add the suite group configuration property to the regex test post processor configurations.
 */
public class AddRegexTestPostProcessorConfigurationDurationGroupUpgradeTask extends AbstractRecordPropertiesUpgradeTask
{
    private static final String TYPE = "zutubi.regexTestPostProcessorConfig";

    private static final String PROPERTY_SUITE_GROUP = "durationGroup";

    protected RecordLocator getRecordLocator()
    {
        return RecordLocators.newTypeFilter(RecordLocators.newPathPattern(
                PathUtils.getPath("projects/*/postProcessors/*")
        ), TYPE);
    }

    protected List<RecordUpgrader> getRecordUpgraders()
    {
        return Arrays.asList(RecordUpgraders.newAddProperty(PROPERTY_SUITE_GROUP, "-1"));
    }

    public boolean haltOnFailure()
    {
        // if the property does not exist in a record, then we end up with the default, which
        // in this case is fine, so no need to fail the upgrade.
        return false;
    }
}