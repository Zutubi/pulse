package com.zutubi.pulse.master.upgrade.tasks;

import com.zutubi.tove.type.record.PathUtils;

import java.util.Arrays;
import java.util.List;

/**
 * Adds the new shortenSuiteNames property to OCUnitTestPostProcessorConfiguration.
 */
public class AddOCUnitShortenSuiteNamesUpgradeTask extends AbstractRecordPropertiesUpgradeTask
{
    private static final String TYPE = "zutubi.ocUnitReportPostProcessorConfig";

    private static final String PROPERTY_SHORTEN_SUITE_NAMES = "shortenSuiteNames";

    protected RecordLocator getRecordLocator()
    {
        return RecordLocators.newTypeFilter(RecordLocators.newPathPattern(
                PathUtils.getPath("projects/*/postProcessors/*")
        ), TYPE);
    }

    protected List<RecordUpgrader> getRecordUpgraders()
    {
        return Arrays.asList(RecordUpgraders.newAddProperty(PROPERTY_SHORTEN_SUITE_NAMES, "false"));
    }

    public boolean haltOnFailure()
    {
        return false;
    }
}
