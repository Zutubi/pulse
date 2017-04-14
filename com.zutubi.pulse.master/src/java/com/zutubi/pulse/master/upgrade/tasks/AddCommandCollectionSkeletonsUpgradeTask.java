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
import com.zutubi.tove.type.record.MutableRecordImpl;
import com.zutubi.tove.type.record.PathUtils;

import java.util.LinkedList;
import java.util.List;

/**
 * Upgrade task to add skeletons for the two collection properties on
 * the executable task and its extensions.
 *
 * The properties are
 * <ul>
 * <li>environments</li>
 * <li>statusMappings</li>
 * </ul>
 *
 * These collections were missed during the MultipeRecipeTypeUpgrade.
 */
public class AddCommandCollectionSkeletonsUpgradeTask extends AbstractRecordPropertiesUpgradeTask
{
    private static final String PROPERTY_ENVIRONMENTS = "environments";
    private static final String PROPERTY_STATUS_MAPPINGS = "statusMappings";

    protected RecordLocator getRecordLocator()
    {
        return RecordLocators.newTypeFilter(
                RecordLocators.newPathPattern("projects/*/type/recipes/*/commands/*"),
                "zutubi.antCommandConfig",
                "zutubi.bjamCommandConfig",
                "zutubi.executableCommandConfig",
                "zutubi.makeCommandConfig",
                "zutubi.mavenCommandConfig",
                "zutubi.maven2CommandConfig",
                "zutubi.msbuildCommandConfig",
                "zutubi.xcodeCommandConfig"
        );
    }

    protected List<? extends RecordUpgrader> getRecordUpgraders()
    {
        List<RecordUpgrader> upgraders = new LinkedList<RecordUpgrader>();
        upgraders.add(new RecordUpgrader()
        {
            public void upgrade(String path, MutableRecord record)
            {
                if (!record.containsKey(PROPERTY_ENVIRONMENTS))
                {
                    recordManager.insert(PathUtils.getPath(path, PROPERTY_ENVIRONMENTS), new MutableRecordImpl());
                }
                if (!record.containsKey(PROPERTY_STATUS_MAPPINGS))
                {
                    recordManager.insert(PathUtils.getPath(path, PROPERTY_STATUS_MAPPINGS), new MutableRecordImpl());
                }
            }
        });
        return upgraders;
    }

    public boolean haltOnFailure()
    {
        return true;
    }
}
