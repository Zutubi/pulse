package com.zutubi.pulse.master.model.persistence.hibernate;

import com.zutubi.pulse.master.model.AgentState;
import com.zutubi.pulse.master.model.AgentSynchronisationMessage;
import com.zutubi.pulse.master.model.persistence.AgentStateDao;
import com.zutubi.pulse.master.model.persistence.AgentSynchronisationMessageDao;
import com.zutubi.pulse.servercore.agent.DeleteDirectoryTask;
import com.zutubi.pulse.servercore.agent.SynchronisationMessage;
import com.zutubi.pulse.servercore.agent.SynchronisationMessageResult;
import com.zutubi.pulse.servercore.agent.SynchronisationTaskFactory;
import com.zutubi.util.bean.DefaultObjectFactory;

import java.util.Collections;

import static java.util.Arrays.asList;

public class HibernateAgentSynchronisationMessageDaoTest extends MasterPersistenceTestCase
{
    private AgentStateDao agentStateDao;
    private AgentSynchronisationMessageDao agentSynchronisationMessageDao;
    private SynchronisationTaskFactory synchronisationTaskFactory;

    public void setUp() throws Exception
    {
        super.setUp();
        agentStateDao = (AgentStateDao) context.getBean("agentStateDao");
        agentSynchronisationMessageDao = (AgentSynchronisationMessageDao) context.getBean("agentSynchronisationMessageDao");
        synchronisationTaskFactory = new SynchronisationTaskFactory();
        synchronisationTaskFactory.setObjectFactory(new DefaultObjectFactory());
    }

    public void testSaveAndLoad()
    {
        AgentState agentState = new AgentState();
        agentStateDao.save(agentState);

        AgentSynchronisationMessage message = new AgentSynchronisationMessage(agentState, synchronisationTaskFactory.toMessage(newTask()), "description");
        message.setStatus(AgentSynchronisationMessage.Status.PROCESSING);
        message.setStatusMessage("nothing to report");

        agentSynchronisationMessageDao.save(message);
        commitAndRefreshTransaction();

        AgentSynchronisationMessage anotherMessage = agentSynchronisationMessageDao.findById(message.getId());

        assertNotSame(message, anotherMessage);
        assertPropertyEquals(message, anotherMessage);
    }

    public void testFindByAgentState()
    {
        AgentState agentState1 = new AgentState();
        AgentState agentState2 = new AgentState();
        agentStateDao.save(agentState1);
        agentStateDao.save(agentState2);

        SynchronisationMessage dummyMessage = synchronisationTaskFactory.toMessage(newTask());

        AgentSynchronisationMessage message11 = new AgentSynchronisationMessage(agentState1, dummyMessage, "desc");
        AgentSynchronisationMessage message12 = new AgentSynchronisationMessage(agentState1, dummyMessage, "desc");
        AgentSynchronisationMessage message21 = new AgentSynchronisationMessage(agentState2, dummyMessage, "desc");
        AgentSynchronisationMessage message22 = new AgentSynchronisationMessage(agentState2, dummyMessage, "desc");

        agentSynchronisationMessageDao.save(message11);
        agentSynchronisationMessageDao.save(message12);
        agentSynchronisationMessageDao.save(message21);
        agentSynchronisationMessageDao.save(message22);

        commitAndRefreshTransaction();

        assertEquals(asList(message11, message12), agentSynchronisationMessageDao.findByAgentState(agentState1));
        assertEquals(asList(message21, message22), agentSynchronisationMessageDao.findByAgentState(agentState2));
    }

    public void testFindByStatus()
    {
        AgentState agentState = new AgentState();
        agentStateDao.save(agentState);

        SynchronisationMessage dummyMessage = synchronisationTaskFactory.toMessage(newTask());

        AgentSynchronisationMessage queuedMessage = new AgentSynchronisationMessage(agentState, dummyMessage, "desc");
        AgentSynchronisationMessage processingMessage1 = new AgentSynchronisationMessage(agentState, dummyMessage, "desc");
        AgentSynchronisationMessage processingMessage2 = new AgentSynchronisationMessage(agentState, dummyMessage, "desc");
        processingMessage1.startProcessing(0);
        processingMessage2.startProcessing(0);
        AgentSynchronisationMessage completeMessage = new AgentSynchronisationMessage(agentState, dummyMessage, "desc");
        completeMessage.applyResult(new SynchronisationMessageResult(0));

        agentSynchronisationMessageDao.save(queuedMessage);
        agentSynchronisationMessageDao.save(processingMessage1);
        agentSynchronisationMessageDao.save(processingMessage2);
        agentSynchronisationMessageDao.save(completeMessage);

        commitAndRefreshTransaction();

        assertEquals(asList(queuedMessage), agentSynchronisationMessageDao.findByStatus(AgentSynchronisationMessage.Status.QUEUED));
        assertEquals(asList(processingMessage1, processingMessage2), agentSynchronisationMessageDao.findByStatus(AgentSynchronisationMessage.Status.PROCESSING));
    }
    
    public void testDeleteByAgentState()
    {
        AgentState agentState1 = new AgentState();
        AgentState agentState2 = new AgentState();
        agentStateDao.save(agentState1);
        agentStateDao.save(agentState2);

        SynchronisationMessage dummyMessage = synchronisationTaskFactory.toMessage(newTask());

        AgentSynchronisationMessage message11 = new AgentSynchronisationMessage(agentState1, dummyMessage, "desc");
        AgentSynchronisationMessage message12 = new AgentSynchronisationMessage(agentState1, dummyMessage, "desc");
        AgentSynchronisationMessage message21 = new AgentSynchronisationMessage(agentState2, dummyMessage, "desc");
        AgentSynchronisationMessage message22 = new AgentSynchronisationMessage(agentState2, dummyMessage, "desc");

        agentSynchronisationMessageDao.save(message11);
        agentSynchronisationMessageDao.save(message12);
        agentSynchronisationMessageDao.save(message21);
        agentSynchronisationMessageDao.save(message22);

        commitAndRefreshTransaction();

        assertEquals(2, agentSynchronisationMessageDao.deleteByAgentState(agentState1));
        assertEquals(0, agentSynchronisationMessageDao.findByAgentState(agentState1).size());
        assertEquals(2, agentSynchronisationMessageDao.findByAgentState(agentState2).size());
    }

    private DeleteDirectoryTask newTask()
    {
        return new DeleteDirectoryTask("foo", "bar", Collections.<String, String>emptyMap());
    }
}