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
import com.zutubi.pulse.master.util.monitor.TaskException;
import com.zutubi.tove.type.record.MutableRecord;
import com.zutubi.tove.type.record.MutableRecordImpl;
import com.zutubi.tove.type.record.PathUtils;
import com.zutubi.tove.type.record.RecordManager;

/**
 * Adds the new "contacts" section to project configuration.
 */
public class AddProjectContactsUpgradeTask extends AbstractUpgradeTask
{
    private static final String SCOPE_PROJECTS = "projects";

    private static final String TYPE_CONTACTS = "zutubi.projectContactsConfig";

    private static final String PROPERTY_USERS = "users";
    private static final String PROPERTY_GROUPS = "groups";
    private static final String PROPERTY_CONTACTS = "contacts";

    private RecordManager recordManager;

    public boolean haltOnFailure()
    {
        return true;
    }

    public void execute() throws TaskException
    {
        TemplatedScopeDetails details = new TemplatedScopeDetails(SCOPE_PROJECTS, recordManager);
        details.getHierarchy().forEach(new Function<ScopeHierarchy.Node, Boolean>()
        {
            public Boolean apply(ScopeHierarchy.Node node)
            {
                MutableRecord contactsRecord = new MutableRecordImpl();
                contactsRecord.setSymbolicName(TYPE_CONTACTS);
                if (node.getParent() == null)
                {
                    contactsRecord.put(PROPERTY_USERS, new String[0]);
                    contactsRecord.put(PROPERTY_GROUPS, new String[0]);
                }
                recordManager.insert(PathUtils.getPath(SCOPE_PROJECTS, node.getId(), PROPERTY_CONTACTS), contactsRecord);
                return true;
            }
        });
    }

    public void setRecordManager(RecordManager recordManager)
    {
        this.recordManager = recordManager;
    }
}
