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
 * Upgrade task that expands the propagateRevision boolean to an revisionHandling enum in dependent
 * triggers.
 */
public class DependentBuildTriggerRevisionHandlingUpgradeTask extends AbstractRecordPropertiesUpgradeTask
{
    private static final String PROPERTY_PROPAGATE_REVISION = "propagateRevision";
    private static final String PROPERTY_REVISION_HANDLING = "revisionHandling";

    public boolean haltOnFailure()
    {
        return true;
    }

    @Override
    protected RecordLocator getRecordLocator()
    {
        return RecordLocators.newTypeFilter(RecordLocators.newPathPattern("projects/*/triggers/*"), "zutubi.dependentBuildTriggerConfig");
    }

    @Override
    protected List<? extends RecordUpgrader> getRecordUpgraders()
    {
        return Arrays.asList(new RecordUpgrader()
        {
            public void upgrade(String path, MutableRecord record)
            {
                if (record.containsKey(PROPERTY_REVISION_HANDLING) || !record.containsKey(PROPERTY_PROPAGATE_REVISION))
                {
                    return;
                }

                Boolean propagate = Boolean.valueOf((String) record.get(PROPERTY_PROPAGATE_REVISION));
                record.put(PROPERTY_REVISION_HANDLING, propagate ? "PROPAGATE_FROM_UPSTREAM" : "FLOAT_INDEPENDENTLY");
            }
        }, RecordUpgraders.newDeleteProperty(PROPERTY_PROPAGATE_REVISION));
    }
}
