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

package com.zutubi.pulse.master.model.persistence.hibernate;

import com.zutubi.pulse.master.model.AgentState;
import com.zutubi.pulse.master.model.persistence.AgentStateDao;

import java.util.List;

public class HibernateAgentStateDaoTest extends MasterPersistenceTestCase
{
    private AgentStateDao agentStateDao;

    public void setUp() throws Exception
    {
        super.setUp();
        agentStateDao = (AgentStateDao) context.getBean("agentStateDao");
    }

    public void testSaveAndLoad()
    {
        AgentState agentState = new AgentState();
        agentStateDao.save(agentState);
        commitAndRefreshTransaction();

        AgentState anotherAgentState = agentStateDao.findById(agentState.getId());

        // want to ensure that a new object has been created by hibernate and
        // that the old one is not just returned to us.
        assertFalse(agentState == anotherAgentState);
        assertPropertyEquals(agentState, anotherAgentState);
    }

    public void testFindAll()
    {
        List slaves = agentStateDao.findAll();
        assertNotNull(slaves);
        assertEquals(0, slaves.size());

        AgentState agentState = new AgentState();
        agentStateDao.save(agentState);
        commitAndRefreshTransaction();

        slaves = agentStateDao.findAll();
        assertNotNull(slaves);
        assertEquals(1, slaves.size());

        agentState = new AgentState();
        agentStateDao.save(agentState);
        commitAndRefreshTransaction();

        slaves = agentStateDao.findAll();
        assertNotNull(slaves);
        assertEquals(2, slaves.size());
    }
}
