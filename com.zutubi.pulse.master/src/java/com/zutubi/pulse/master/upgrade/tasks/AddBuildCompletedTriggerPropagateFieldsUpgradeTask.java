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

import com.zutubi.tove.type.record.PathUtils;

import java.util.Arrays;
import java.util.List;

import static com.zutubi.pulse.master.upgrade.tasks.RecordUpgraders.newAddProperty;

/**
 * Upgrade task that adds the propagate status and version fields to the build completed trigger configuration.
 */
public class AddBuildCompletedTriggerPropagateFieldsUpgradeTask extends AbstractRecordPropertiesUpgradeTask
{
    private static final String BUILD_COMPLETED_TRIGGER_SYMBOLIC_NAME = "zutubi.buildCompletedConfig";

    private static final String PROPERTY_PROPAGATE_STATUS = "propagateStatus";

    private static final String DEFAULT_PROPAGATE_STATUS = "false";

    private static final String PROPERTY_PROPAGATE_VERSION = "propagateVersion";

    private static final String DEFAULT_PROPAGATE_VERSION = "false";

    protected RecordLocator getRecordLocator()
    {
        return RecordLocators.newTypeFilter (
                RecordLocators.newPathPattern(PathUtils.getPath("projects", PathUtils.WILDCARD_ANY_ELEMENT, "triggers")),
                BUILD_COMPLETED_TRIGGER_SYMBOLIC_NAME
        );
    }

    protected List<? extends RecordUpgrader> getRecordUpgraders()
    {
        return Arrays.asList(newAddProperty(PROPERTY_PROPAGATE_STATUS, DEFAULT_PROPAGATE_STATUS),
                newAddProperty(PROPERTY_PROPAGATE_VERSION, DEFAULT_PROPAGATE_VERSION));
    }

    public boolean haltOnFailure()
    {
        return true;
    }
}