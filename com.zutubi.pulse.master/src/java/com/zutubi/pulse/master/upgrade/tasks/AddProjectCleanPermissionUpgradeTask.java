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
import com.zutubi.util.CollectionUtils;

import java.util.Arrays;
import java.util.List;

/**
 * Adds a separate permission for project cleanup, updating ACLs that have the
 * existing trigger permission.
 */
public class AddProjectCleanPermissionUpgradeTask extends AbstractRecordPropertiesUpgradeTask
{
    public boolean haltOnFailure()
    {
        return true;
    }

    @Override
    protected RecordLocator getRecordLocator()
    {
        return RecordLocators.newPathPattern("projects/*/permissions/*");
    }

    @Override
    protected List<? extends RecordUpgrader> getRecordUpgraders()
    {
        return Arrays.asList(RecordUpgraders.newEditProperty("allowedActions", new Function<Object, Object>()
        {
            public Object apply(Object o)
            {
                if (o != null && o instanceof String[])
                {
                    String[] allowedActions = (String[]) o;
                    if (CollectionUtils.contains(allowedActions, "trigger"))
                    {
                        String[] editedActions = new String[allowedActions.length + 1];
                        System.arraycopy(allowedActions, 0, editedActions, 0, allowedActions.length);
                        editedActions[editedActions.length - 1] = "clean";
                        o = editedActions;
                    }
                }

                return o;
            }
        }));
    }
}
