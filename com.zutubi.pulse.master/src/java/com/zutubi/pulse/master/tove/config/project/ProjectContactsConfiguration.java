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

package com.zutubi.pulse.master.tove.config.project;

import com.zutubi.pulse.master.tove.config.group.GroupConfiguration;
import com.zutubi.pulse.master.tove.config.user.UserConfiguration;
import com.zutubi.tove.annotations.Form;
import com.zutubi.tove.annotations.Reference;
import com.zutubi.tove.annotations.SymbolicName;
import com.zutubi.tove.config.api.AbstractConfiguration;

import java.util.LinkedList;
import java.util.List;

/**
 * Configures the contact people for a project.  These people may be notified
 * of project events/builds/etc.
 */
@SymbolicName("zutubi.projectContactsConfig")
@Form(fieldOrder = {"groups", "users"})
public class ProjectContactsConfiguration extends AbstractConfiguration
{
    @Reference
    private List<GroupConfiguration> groups = new LinkedList<GroupConfiguration>();
    @Reference
    private List<UserConfiguration> users = new LinkedList<UserConfiguration>();

    public List<GroupConfiguration> getGroups()
    {
        return groups;
    }

    public void setGroups(List<GroupConfiguration> groups)
    {
        this.groups = groups;
    }

    public List<UserConfiguration> getUsers()
    {
        return users;
    }

    public void setUsers(List<UserConfiguration> users)
    {
        this.users = users;
    }
}
