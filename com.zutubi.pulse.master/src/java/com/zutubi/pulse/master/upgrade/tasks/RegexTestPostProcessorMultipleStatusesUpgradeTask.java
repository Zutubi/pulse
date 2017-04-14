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
import com.zutubi.tove.type.record.PathUtils;

import java.util.List;

import static java.util.Arrays.asList;

/**
 * Updates regex-test post-processors to allow them to have multiple strings
 * for each test status.
 */
public class RegexTestPostProcessorMultipleStatusesUpgradeTask extends AbstractRecordPropertiesUpgradeTask
{
    private static final String TYPE = "zutubi.regexTestPostProcessorConfig";

    private static final String PROPERTY_PASS_STATUS = "passStatus";
    private static final String PROPERTY_FAILURE_STATUS = "failureStatus";
    private static final String PROPERTY_ERROR_STATUS = "errorStatus";
    private static final String PROPERTY_SKIPPED_STATUS = "skippedStatus";

    protected RecordLocator getRecordLocator()
    {
        return RecordLocators.newTypeFilter(RecordLocators.newPathPattern(
                PathUtils.getPath("projects/*/postProcessors/*")
        ), TYPE);
    }

    protected List<? extends RecordUpgrader> getRecordUpgraders()
    {
        ToArrayFn editFn = new ToArrayFn();
        return asList(
                RecordUpgraders.newEditProperty(PROPERTY_PASS_STATUS, editFn),
                RecordUpgraders.newEditProperty(PROPERTY_FAILURE_STATUS, editFn),
                RecordUpgraders.newEditProperty(PROPERTY_ERROR_STATUS, editFn),
                RecordUpgraders.newEditProperty(PROPERTY_SKIPPED_STATUS, editFn)
                
        );
    }

    public boolean haltOnFailure()
    {
        return true;
    }

    private static class ToArrayFn implements Function<Object, Object>
    {
        public Object apply(Object o)
        {
            if (o == null)
            {
                return null;
            }
            else if (o instanceof String)
            {
                return new String[]{(String) o};
            }
            else
            {
                return o;
            }
        }
    }
}