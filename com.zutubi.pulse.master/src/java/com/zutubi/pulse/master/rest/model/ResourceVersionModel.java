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

package com.zutubi.pulse.master.rest.model;

import java.util.Set;
import java.util.TreeSet;

/**
 * Models a resource version, collecting which agents have the version.
 */
public class ResourceVersionModel
{
    private String versionId;
    private Set<String> agents;

    public ResourceVersionModel(String versionId)
    {
        this.versionId = versionId;
    }

    public String getVersionId()
    {
        return versionId;
    }

    public Set<String> getAgents()
    {
        return agents;
    }

    public void addAgent(String name)
    {
        if (agents == null)
        {
            agents = new TreeSet<>();
        }

        agents.add(name);
    }
}
