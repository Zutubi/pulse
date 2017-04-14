/* Copyright 2017 Zutubi Pty Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.zutubi.pulse.master.upgrade.tasks;

import com.google.common.base.Function;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;
import com.zutubi.tove.type.record.PathUtils;
import com.zutubi.util.StringUtils;

import java.util.List;

import static java.util.Arrays.asList;

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

    protected List<? extends RecordUpgrader> getRecordUpgraders()
    {
        FixColumnsFn editFn = new FixColumnsFn();
        return asList(RecordUpgraders.newDeleteProperty("myProjectsColumns"),
                RecordUpgraders.newDeleteProperty("projectSummaryColumns"),
                RecordUpgraders.newEditProperty("myBuildsColumns", editFn),
                RecordUpgraders.newEditProperty("projectRecentColumns", editFn),
                RecordUpgraders.newEditProperty("projectHistoryColumns", editFn));
    }

    private static class FixColumnsFn implements Function<Object, Object>
    {
        public Object apply(Object o)
        {
            if (o != null && o instanceof String)
            {
                String columnsString = (String) o;
                Iterable<String> columns = asList(StringUtils.split(columnsString, ',', true));
                columns = Iterables.filter(columns, Predicates.not(Predicates.equalTo(COLUMN_ACTIONS)));
                columns = Iterables.transform(columns, new Function<String, String>()
                {
                    public String apply(String s)
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
