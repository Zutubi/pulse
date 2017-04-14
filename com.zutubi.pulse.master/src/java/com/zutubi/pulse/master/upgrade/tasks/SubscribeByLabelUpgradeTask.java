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

import com.zutubi.tove.type.record.MutableRecord;

import java.util.Arrays;
import java.util.List;

/**
 * Upgrade task to add the ability to explicitly subscribe to all projects
 * and/or subscribe by label.
 */
public class SubscribeByLabelUpgradeTask extends AbstractRecordPropertiesUpgradeTask
{
    private static final String PROPERTY_ALL_PROJECTS = "allProjects";
    private static final String PROPERTY_PROJECTS = "projects";
    private static final String PROPERTY_LABELS = "labels";

    public boolean haltOnFailure()
    {
        return false;
    }

    protected RecordLocator getRecordLocator()
    {
        return RecordLocators.newTypeFilter(RecordLocators.newPathPattern("users/*/preferences/subscriptions/*"), "zutubi.projectSubscriptionConfig");
    }

    protected List<? extends RecordUpgrader> getRecordUpgraders()
    {
        return Arrays.<RecordUpgrader>asList(new RecordUpgrader()
        {
            public void upgrade(String path, MutableRecord record)
            {
                if (record.containsKey(PROPERTY_ALL_PROJECTS))
                {
                    return;
                }

                String[] projects = (String[]) record.get(PROPERTY_PROJECTS);
                boolean allProjectsValue = projects.length == 0;
                record.put(PROPERTY_ALL_PROJECTS, Boolean.toString(allProjectsValue));
                record.put(PROPERTY_LABELS, new String[0]);
            }
        });
    }
}