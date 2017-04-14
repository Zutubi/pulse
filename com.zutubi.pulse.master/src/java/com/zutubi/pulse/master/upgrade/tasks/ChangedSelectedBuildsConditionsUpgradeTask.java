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
 * Upgrades selected builds subscription conditions for new changed(...) syntax.
 */
public class ChangedSelectedBuildsConditionsUpgradeTask extends AbstractRecordPropertiesUpgradeTask
{
    private static final String PROPERTY_INCLUDE_CHANGES = "includeChanges";
    private static final String PROPERTY_INCLUDE_CHANGES_BY_ME = "includeChangesByMe";
    private static final String PROPERTY_CHANGES_BY_ME = "changesByMe";
    private static final String PROPERTY_CHANGES_SINCE_HEALTHY = "changesSinceHealthy";
    private static final String PROPERTY_CHANGES_SINCE_SUCCESS = "changesSinceSuccess";
    private static final String PROPERTY_UPSTREAM_CHANGES = "upstreamChanges";

    @Override
    protected RecordLocator getRecordLocator()
    {
        return RecordLocators.newTypeFilter(
                RecordLocators.newPathPattern("users/*/preferences/subscriptions/*/condition"),
                "zutubi.selectedBuildsConditionConfig"
        );
    }

    @Override
    protected List<? extends RecordUpgrader> getRecordUpgraders()
    {
        RecordUpgrader updateChangesProperty = new RecordUpgrader()
        {
            public void upgrade(String path, MutableRecord record)
            {
                boolean includeChanges = propertyIsTrue(record, PROPERTY_INCLUDE_CHANGES) || propertyIsTrue(record, PROPERTY_INCLUDE_CHANGES_BY_ME);
                record.put(PROPERTY_INCLUDE_CHANGES, Boolean.toString(includeChanges));
            }
        };

        return Arrays.asList(updateChangesProperty,
                             RecordUpgraders.newRenameProperty(PROPERTY_INCLUDE_CHANGES_BY_ME, PROPERTY_CHANGES_BY_ME),
                             RecordUpgraders.newAddProperty(PROPERTY_CHANGES_SINCE_HEALTHY, "false"),
                             RecordUpgraders.newAddProperty(PROPERTY_CHANGES_SINCE_SUCCESS, "false"),
                             RecordUpgraders.newAddProperty(PROPERTY_UPSTREAM_CHANGES, "false"));
    }

    private boolean propertyIsTrue(MutableRecord record, String property)
    {
        Object value = record.get(property);
        return value != null && value instanceof String && Boolean.parseBoolean((String) value);
    }

    public boolean haltOnFailure()
    {
        return false;
    }
}
