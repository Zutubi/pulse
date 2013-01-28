package com.zutubi.pulse.master.upgrade.tasks;

import com.google.common.base.Predicate;
import com.zutubi.tove.type.record.Record;

import java.util.Arrays;
import java.util.List;

/**
 * Adds inclusion paths to pollable scm configurations, and renames the
 * existing filterPaths to excludedPaths to avoid confusion.
 */
public class ScmInclusionPathsUpgradeTask extends AbstractRecordPropertiesUpgradeTask
{
    private static final String PROPERTY_FILTER_PATHS = "filterPaths";
    private static final String PROPERTY_INCLUDED_PATHS = "includedPaths";
    private static final String PROPERTY_EXCLUDED_PATHS = "excludedPaths";

    public boolean haltOnFailure()
    {
        return true;
    }

    protected RecordLocator getRecordLocator()
    {
        return RecordLocators.newPredicateFilter(RecordLocators.newPathPattern("projects/*/scm"), new Predicate<Record>()
        {
            public boolean apply(Record record)
            {
                return record.containsKey(PROPERTY_FILTER_PATHS);
            }
        });
    }

    protected List<RecordUpgrader> getRecordUpgraders()
    {
        return Arrays.asList(
                RecordUpgraders.newAddProperty(PROPERTY_INCLUDED_PATHS, new String[0]),
                RecordUpgraders.newRenameProperty(PROPERTY_FILTER_PATHS, PROPERTY_EXCLUDED_PATHS)
        );
    }
}