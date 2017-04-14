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

/**
 * Add the suite group configuration property to the regex test post processor configurations.
 */
public class AddRegexTestPostProcessorConfigurationSuiteGroupUpgradeTask  extends AbstractRecordPropertiesUpgradeTask
{
    private static final String TYPE = "zutubi.regexTestPostProcessorConfig";

    private static final String PROPERTY_SUITE_GROUP = "suiteGroup";

    protected RecordLocator getRecordLocator()
    {
        return RecordLocators.newTypeFilter(RecordLocators.newPathPattern(
                PathUtils.getPath("projects/*/postProcessors/*")
        ), TYPE);
    }

    protected List<? extends RecordUpgrader> getRecordUpgraders()
    {
        return Arrays.asList(RecordUpgraders.newAddProperty(PROPERTY_SUITE_GROUP, "-1"));
    }

    public boolean haltOnFailure()
    {
        // if the property does not exist in a record, then we end up with the default, which
        // in this case is fine, so no need to fail the upgrade.
        return false;
    }
}
