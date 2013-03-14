package com.zutubi.pulse.master.upgrade.tasks;

import com.google.common.base.Function;
import com.zutubi.tove.type.record.PathUtils;

import java.util.Arrays;
import java.util.List;

/**
 * Updates the range label of the default build/stage time report if possible.
 */
public class UpdateTimeReportLabelUpgradeTask extends AbstractRecordPropertiesUpgradeTask
{
    private static final String SCOPE_PROJECTS = "projects";
    private static final String PATH_REPORT = "reportGroups/build trends/reports/average elapsed times";
    private static final String PROPERTY_LABEL = "rangeLabel";
    private static final String ORIGINAL_SUFFIX = " (seconds)";

    protected RecordLocator getRecordLocator()
    {
        TemplatedScopeDetails scopeDetails = (TemplatedScopeDetails) persistentScopes.getScopeDetails(SCOPE_PROJECTS);
        String globalProject = scopeDetails.getHierarchy().getRoot().getId();
        return RecordLocators.newPath(PathUtils.getPath(SCOPE_PROJECTS, globalProject, PATH_REPORT));
    }

    protected List<? extends RecordUpgrader> getRecordUpgraders()
    {
        return Arrays.asList(RecordUpgraders.newEditProperty(PROPERTY_LABEL, new Function<Object, Object>()
        {
            public Object apply(Object currentValue)
            {
                if (currentValue != null && currentValue instanceof String)
                {
                    String stringValue = (String) currentValue;
                    if (stringValue.endsWith(ORIGINAL_SUFFIX))
                    {
                        return stringValue.substring(0, stringValue.length() - ORIGINAL_SUFFIX.length());
                    }
                }
                
                return currentValue;
            }
        }));
    }

    public boolean haltOnFailure()
    {
        return false;
    }
}