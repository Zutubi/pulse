package com.zutubi.pulse.master.upgrade.tasks;

import com.zutubi.tove.type.record.PathUtils;
import com.zutubi.util.*;

import java.util.Arrays;
import java.util.List;

/**
 * Upgrade task to add two properties to the users dashboard configuration.  The sortProjectsAlphabetically
 * and the sortGroupsAlphabetically.  Both default to true.
 */
public class UpdateBuildColumnPreferencesUpgradeTask extends AbstractRecordPropertiesUpgradeTask
{
    private static final String SCOPE_USERS = "users";
    private static final String PROPERTY_PREFERENCES = "preferences";
    private static final String COLUMN_ACTIONS = "actions";
    private static final String COLUMN_NUMBER_ORIGINAL = "id";
    private static final String COLUMN_NUMBER_NEW = "number";
    private static final String COLUMN_REVISION_ORIGINAL = "rev";
    private static final String COLUMN_REVISION_NEW = "revision";

    public boolean haltOnFailure()
    {
        return true;
    }

    protected RecordLocator getRecordLocator()
    {
        return RecordLocators.newPathPattern(PathUtils.getPath(SCOPE_USERS, PathUtils.WILDCARD_ANY_ELEMENT, PROPERTY_PREFERENCES));
    }

    protected List<RecordUpgrader> getRecordUpgraders()
    {
        FixColumnsFn editFn = new FixColumnsFn();
        return Arrays.asList(RecordUpgraders.newDeleteProperty("myProjectsColumns"),
                             RecordUpgraders.newDeleteProperty("projectSummaryColumns"),
                             RecordUpgraders.newEditProperty("myBuildsColumns", editFn),
                             RecordUpgraders.newEditProperty("projectRecentColumns", editFn),
                             RecordUpgraders.newEditProperty("projectHistoryColumns", editFn));
    }

    private static class FixColumnsFn implements UnaryFunction<Object, Object>
    {
        public Object process(Object o)
        {
            if (o != null && o instanceof String)
            {
                String columnsString = (String) o;
                String[] columns = StringUtils.split(columnsString, ',', true);
                columns = CollectionUtils.filterToArray(columns, new Predicate<String>()
                {
                    public boolean satisfied(String s)
                    {
                        return !s.equals(COLUMN_ACTIONS);
                    }
                });
                
                columns = CollectionUtils.mapToArray(columns, new Mapping<String, String>()
                {
                    public String map(String s)
                    {
                        if (COLUMN_NUMBER_ORIGINAL.equals(s))
                        {
                            return COLUMN_NUMBER_NEW;
                        }
                        else if (COLUMN_REVISION_ORIGINAL.equals(s))
                        {
                            return COLUMN_REVISION_NEW;
                        }
                        else
                        {
                            return s;
                        }
                    }
                });
                
                return StringUtils.join(",", columns);
            }
            
            return o;
        }
    }
}
