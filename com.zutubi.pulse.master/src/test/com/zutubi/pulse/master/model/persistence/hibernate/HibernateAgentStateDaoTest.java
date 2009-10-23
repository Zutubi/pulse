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
