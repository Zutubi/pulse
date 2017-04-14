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

import java.util.Arrays;
import java.util.List;

/**
 * Updates the project persistentWorkDir property to allow for the new
 * ${agent.data.dir} property (in place of ${data.dir}, which is still
 * available but no longer the default place for agent data).
 */
public class UpdatePersistentWorkDirUpgradeTask extends AbstractRecordPropertiesUpgradeTask
{
    private static final String SCOPE = "projects";
    private static final String PROPERTY_OPTIONS = "options";
    private static final String PROPERTY_PERSISTENT_WORK_DIR = "persistentWorkDir";

    public boolean haltOnFailure()
    {
        return false;
    }

    protected RecordLocator getRecordLocator()
    {
        return RecordLocators.newPathPattern(PathUtils.getPath(SCOPE, PathUtils.WILDCARD_ANY_ELEMENT, PROPERTY_OPTIONS));
    }

    protected List<? extends RecordUpgrader> getRecordUpgraders()
    {
        return Arrays.asList(RecordUpgraders.newEditProperty(PROPERTY_PERSISTENT_WORK_DIR, new Function<Object, Object>()
        {
            public Object apply(Object o)
            {
                if (o == null)
                {
                    return null;
                }

                String current = (String) o;
                return current.replace("${data.dir}", "${agent.data.dir}");
            }
        }));
    }
}
