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
 * Upgrade task to add two new properties to the perforce configuration,
 * the unicodeServer flag and the charset field.
 */
public class AddPerforceUnicodeConfigurationUpgradeTask extends AbstractRecordPropertiesUpgradeTask
{
    private static final String PROPERTY_UNICODE_SERVER = "unicodeServer";
    private static final String PROPERTY_CHARSET = "charset";
    private static final String SCOPE_PROJECTS = "projects";
    private static final String PROPERTY_SCM = "scm";
    private static final String TYPE_PERFORCE = "zutubi.perforceConfig";

    public boolean haltOnFailure()
    {
        return true;
    }

    protected RecordLocator getRecordLocator()
    {
        RecordLocator locator = RecordLocators.newPathPattern(PathUtils.getPath(SCOPE_PROJECTS, PathUtils.WILDCARD_ANY_ELEMENT, PROPERTY_SCM));
        return RecordLocators.newTypeFilter(locator, TYPE_PERFORCE);
    }

    protected List<? extends RecordUpgrader> getRecordUpgraders()
    {
        return Arrays.asList(
                RecordUpgraders.newAddProperty(PROPERTY_UNICODE_SERVER, Boolean.toString(false)),
                RecordUpgraders.newAddProperty(PROPERTY_CHARSET, "none")
        );
    }

}
