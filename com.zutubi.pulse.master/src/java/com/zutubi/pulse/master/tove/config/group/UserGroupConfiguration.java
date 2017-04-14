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

package com.zutubi.pulse.master.tove.config.group;

import com.zutubi.pulse.master.tove.config.user.UserConfiguration;
import com.zutubi.tove.annotations.*;

import java.util.LinkedList;
import java.util.List;

/**
 * Represents a group of users.  Groups are used to conveniently assign
 * permissions to multiple users.
 */
@SymbolicName("zutubi.groupConfig")
@Form(fieldOrder = {"name", "members", "serverPermissions", "concurrentPersonalBuilds"})
@Classification(single = "group")
public class UserGroupConfiguration extends GroupConfiguration
{
    @Reference @Ordered(allowReordering = false)
    private List<UserConfiguration> members = new LinkedList<UserConfiguration>();

    public UserGroupConfiguration()
    {
    }

    public UserGroupConfiguration(String name)
    {
        super(name);
    }

    public List<UserConfiguration> getMembers()
    {
        return members;
    }

    public void setMembers(List<UserConfiguration> members)
    {
        this.members = members;
    }

    @Transient
    public String getDefaultAuthority()
    {
        return "group:" + getName();
    }
}
