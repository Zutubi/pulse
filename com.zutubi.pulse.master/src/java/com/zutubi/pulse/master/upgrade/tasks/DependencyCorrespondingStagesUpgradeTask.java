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
import com.zutubi.tove.type.record.PathUtils;

import java.util.List;

import static java.util.Arrays.asList;

/**
 * Converts the allStages boolean flag in dependency configuration to a three-
 * way enumeration.
 */
public class DependencyCorrespondingStagesUpgradeTask extends AbstractRecordPropertiesUpgradeTask
{
    private static final String SCOPE_PROJECTS = "projects";
    private static final String PROPERTY_DEPENDENCIES = "dependencies";
    private static final String PROPERTY_ALL_STAGES = "allStages";
    private static final String PROPERTY_STAGE_TYPE = "stageType";

    private static final String VALUE_ALL_STAGES = "ALL_STAGES";
    private static final String VALUE_SELECTED_STAGES = "SELECTED_STAGES";

    public boolean haltOnFailure()
    {
        return true;
    }

    @Override
    protected RecordLocator getRecordLocator()
    {
        return RecordLocators.newPathPattern(PathUtils.getPath(SCOPE_PROJECTS, PathUtils.WILDCARD_ANY_ELEMENT, PROPERTY_DEPENDENCIES, PROPERTY_DEPENDENCIES, PathUtils.WILDCARD_ANY_ELEMENT));
    }

    @Override
    protected List<? extends RecordUpgrader> getRecordUpgraders()
    {
        RecordUpgrader upgrader = new RecordUpgrader()
        {
            public void upgrade(String path, MutableRecord record)
            {
                String allStages = (String) record.remove(PROPERTY_ALL_STAGES);
                record.put(PROPERTY_STAGE_TYPE, Boolean.valueOf(allStages)? VALUE_ALL_STAGES : VALUE_SELECTED_STAGES);
            }
        };

        return asList(upgrader);
    }
}
