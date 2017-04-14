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

import java.util.List;

import static com.zutubi.pulse.master.upgrade.tasks.RecordLocators.newPathPattern;
import static com.zutubi.pulse.master.upgrade.tasks.RecordLocators.newTypeFilter;
import static com.zutubi.pulse.master.upgrade.tasks.RecordUpgraders.newAddProperty;
import static com.zutubi.tove.type.record.PathUtils.WILDCARD_ANY_ELEMENT;
import static com.zutubi.tove.type.record.PathUtils.getPath;
import static java.util.Arrays.asList;

/**
 * Adds the new hashing options to file system artifacts.
 */
public class AddArtifactHashUpgradeTask extends AbstractRecordPropertiesUpgradeTask
{
    public boolean haltOnFailure()
    {
        return true;
    }

    protected RecordLocator getRecordLocator()
    {
        String allArtifactsPattern = getPath("projects", WILDCARD_ANY_ELEMENT, "type", "recipes", WILDCARD_ANY_ELEMENT, "commands", WILDCARD_ANY_ELEMENT, "artifacts", WILDCARD_ANY_ELEMENT);
        return newTypeFilter(newPathPattern(allArtifactsPattern),
                             "zutubi.fileArtifactConfig", "zutubi.directoryArtifactConfig");
    }

    protected List<? extends RecordUpgrader> getRecordUpgraders()
    {
        return asList(
                newAddProperty("calculateHash", Boolean.toString(false)),
                newAddProperty("hashAlgorithm", "MD5")
        );
    }
}