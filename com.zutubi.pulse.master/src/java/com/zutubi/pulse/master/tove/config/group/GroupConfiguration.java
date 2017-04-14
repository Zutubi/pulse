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

import com.zutubi.tove.annotations.Classification;
import com.zutubi.tove.annotations.SymbolicName;
import com.zutubi.tove.annotations.Table;
import com.zutubi.tove.annotations.Transient;
import com.zutubi.tove.config.api.AbstractNamedConfiguration;
import com.zutubi.validation.annotations.Min;

import java.util.LinkedList;
import java.util.List;

/**
 */
@SymbolicName("zutubi.abstractGroupConfig")
@Table(columns = {"name", "members"})
@Classification(collection = "users")
public abstract class GroupConfiguration extends AbstractNamedConfiguration
{
    private List<ServerPermission> serverPermissions = new LinkedList<ServerPermission>();
    @Min(1)
    private int concurrentPersonalBuilds = 1;

    public GroupConfiguration()
    {
    }

    public GroupConfiguration(String name)
    {
        super(name);
    }

    public List<ServerPermission> getServerPermissions()
    {
        return serverPermissions;
    }

    public void setServerPermissions(List<ServerPermission> serverPermissions)
    {
        this.serverPermissions = serverPermissions;
    }

    public void addServerPermission(ServerPermission permission)
    {
        serverPermissions.add(permission);
    }

    public int getConcurrentPersonalBuilds()
    {
        return concurrentPersonalBuilds;
    }

    public void setConcurrentPersonalBuilds(int concurrentPersonalBuilds)
    {
        this.concurrentPersonalBuilds = concurrentPersonalBuilds;
    }

    @Transient
    public String[] getGrantedAuthorities()
    {
        String[] result = new String[serverPermissions.size() + 1];
        int i = 0;
        for(ServerPermission perm: serverPermissions)
        {
            result[i++] = perm.toString();
        }

        result[i] = getDefaultAuthority();
        return result;
    }

    @Transient
    public abstract String getDefaultAuthority();
}
