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

package com.zutubi.pulse.master.tove.config.admin;

import com.zutubi.tove.annotations.Classification;
import com.zutubi.tove.annotations.SymbolicName;
import com.zutubi.tove.annotations.Reference;
import com.zutubi.tove.config.api.AbstractConfiguration;
import com.zutubi.pulse.master.tove.config.group.GroupConfiguration;

import java.util.List;
import java.util.LinkedList;

/**
 * The configuration that controls the embedded artifact repository.
 */
@SymbolicName("zutubi.repositoryConfig")
@Classification(single = "repository")
public class RepositoryConfiguration extends AbstractConfiguration
{
    /**
     * The list of groups that by default have read access to the repository.
     */
    @Reference
    private List<GroupConfiguration> readAccess = new LinkedList<GroupConfiguration>();

    /**
     * The list of groups that by default have write access to the repository.
     */
    @Reference
    private List<GroupConfiguration> writeAccess = new LinkedList<GroupConfiguration>();

    public List<GroupConfiguration> getReadAccess()
    {
        return readAccess;
    }

    public void setReadAccess(List<GroupConfiguration> readAccess)
    {
        this.readAccess = readAccess;
    }

    public List<GroupConfiguration> getWriteAccess()
    {
        return writeAccess;
    }

    public void setWriteAccess(List<GroupConfiguration> writeAccess)
    {
        this.writeAccess = writeAccess;
    }
}
