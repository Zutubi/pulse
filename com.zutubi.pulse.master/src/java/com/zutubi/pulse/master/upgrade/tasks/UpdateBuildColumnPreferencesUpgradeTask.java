package com.zutubi.pulse.master.upgrade.tasks;

import com.zutubi.tove.type.record.PathUtils;
import com.zutubi.util.CollectionUtils;
import com.zutubi.util.Predicate;
import com.zutubi.util.StringUtils;
import com.zutubi.util.UnaryFunction;

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
        RemoveActionsColumnFn editFn = new RemoveActionsColumnFn();
        return Arrays.asList(RecordUpgraders.newDeleteProperty("myProjectsColumns"),
                             RecordUpgraders.newDeleteProperty("projectSummaryColumns"),
                             RecordUpgraders.newEditProperty("myBuildsColumns", editFn),
                             RecordUpgraders.newEditProperty("projectRecentColumns", editFn),
                             RecordUpgraders.newEditProperty("projectHistoryColumns", editFn));
    }

    private static class RemoveActionsColumnFn implements UnaryFunction<Object, Object>
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
                
                return StringUtils.join(",", columns);
            }
            
            return o;
        }
    }
}
