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

import com.google.common.base.Predicate;
import com.zutubi.tove.type.record.Record;

import java.util.Arrays;
import java.util.List;

/**
 * Adds inclusion paths to pollable scm configurations, and renames the
 * existing filterPaths to excludedPaths to avoid confusion.
 */
public class ScmInclusionPathsUpgradeTask extends AbstractRecordPropertiesUpgradeTask
{
    private static final String PROPERTY_FILTER_PATHS = "filterPaths";
    private static final String PROPERTY_INCLUDED_PATHS = "includedPaths";
    private static final String PROPERTY_EXCLUDED_PATHS = "excludedPaths";

    public boolean haltOnFailure()
    {
        return true;
    }

    protected RecordLocator getRecordLocator()
    {
        return RecordLocators.newPredicateFilter(RecordLocators.newPathPattern("projects/*/scm"), new Predicate<Record>()
        {
            public boolean apply(Record record)
            {
                return record.containsKey(PROPERTY_FILTER_PATHS);
            }
        });
    }

    protected List<? extends RecordUpgrader> getRecordUpgraders()
    {
        return Arrays.asList(
                RecordUpgraders.newAddProperty(PROPERTY_INCLUDED_PATHS, new String[0]),
                RecordUpgraders.newRenameProperty(PROPERTY_FILTER_PATHS, PROPERTY_EXCLUDED_PATHS)
        );
    }
}