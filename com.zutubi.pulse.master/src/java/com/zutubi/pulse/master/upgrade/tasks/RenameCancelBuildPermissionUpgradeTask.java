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

import java.util.List;

import static com.zutubi.tove.type.record.PathUtils.WILDCARD_ANY_ELEMENT;
import static com.zutubi.tove.type.record.PathUtils.getPath;
import static java.util.Arrays.asList;

/**
 * Updates the value for cancel build permissions to be camel-cased, so we can
 * give it a proper i18n label.
 */
public class RenameCancelBuildPermissionUpgradeTask extends AbstractRecordPropertiesUpgradeTask
{
    private static final String SCOPE_PROJECTS = "projects";
    private static final String PROPERTY_PERMISSIONS = "permissions";
    private static final String PROPERTY_ALLOWED_ACTIONS = "allowedActions";
    private static final String PERMISSION_ORIGINAL = "cancel build";
    private static final String PERMISSION_NEW = "cancelBuild";

    protected RecordLocator getRecordLocator()
    {
        return RecordLocators.newPathPattern(getPath(SCOPE_PROJECTS, WILDCARD_ANY_ELEMENT, PROPERTY_PERMISSIONS, WILDCARD_ANY_ELEMENT));
    }

    protected List<? extends RecordUpgrader> getRecordUpgraders()
    {
        return asList(
                RecordUpgraders.newEditProperty(PROPERTY_ALLOWED_ACTIONS, new Function<Object, Object>()
                {
                    public Object apply(Object o)
                    {
                        if (o != null && o instanceof String[])
                        {
                            String[] permissions = (String[]) o;
                            for (int i = 0; i < permissions.length; i++)
                            {
                                if (PERMISSION_ORIGINAL.equals(permissions[i]))
                                {
                                    permissions[i] = PERMISSION_NEW;
                                }
                            }
                        }

                        return o;
                    }
                })

        );
    }

    public boolean haltOnFailure()
    {
        return true;
    }
}