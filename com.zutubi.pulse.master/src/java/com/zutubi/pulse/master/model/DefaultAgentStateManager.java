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

package com.zutubi.pulse.master.model;

import com.zutubi.pulse.master.model.persistence.AgentStateDao;

import java.util.List;

/**
 */
public class DefaultAgentStateManager implements AgentStateManager
{
    private AgentStateDao agentStateDao;
    private ProjectManager projectManager;

    public void init()
    {
    }

    public List<AgentState> getAll()
    {
        return agentStateDao.findAll();
    }

    public AgentState getAgentState(long id)
    {
        return agentStateDao.findById(id);
    }

    public void delete(long id)
    {
    }

    public void delete(AgentState agentState)
    {
        agentStateDao.delete(agentState);
    }

    public void save(AgentState agentState)
    {
        agentStateDao.save(agentState);
    }

    public void setAgentStateDao(AgentStateDao agentStateDao)
    {
        this.agentStateDao = agentStateDao;
    }

    public void setProjectManager(ProjectManager projectManager)
    {
        this.projectManager = projectManager;
    }
}