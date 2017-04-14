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
import com.google.common.base.Predicate;
import com.zutubi.tove.type.record.Record;

import java.util.Arrays;
import java.util.List;

/**
 * Updates test post-processor's resolveConflicts property to reflect the branching of OFF into multiple options.
 */
public class UpdateNameConflictResolutionUpgradeTask extends AbstractRecordPropertiesUpgradeTask
{
    private static final String PROPERTY = "resolveConflicts";

    public boolean haltOnFailure()
    {
        return true;
    }

    @Override
    protected RecordLocator getRecordLocator()
    {
        return RecordLocators.newPredicateFilter(RecordLocators.newPathPattern("projects/*/postProcessors/*"), new Predicate<Record>()
        {
            public boolean apply(Record input)
            {
                return input.containsKey(PROPERTY);
            }
        });
    }

    @Override
    protected List<? extends RecordUpgrader> getRecordUpgraders()
    {
        return Arrays.asList(RecordUpgraders.newEditProperty(PROPERTY, new Function<Object, Object>()
        {
            public Object apply(Object input)
            {
                if (input != null && input instanceof String)
                {
                    String value = (String) input;
                    if (value.equals("OFF"))
                    {
                        return "WORST_RESULT";
                    }
                }

                return input;
            }
        }));
    }
}
