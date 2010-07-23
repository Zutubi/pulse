package com.zutubi.pulse.master.agent;

import com.zutubi.events.DefaultEventManager;
import com.zutubi.events.EventManager;
import com.zutubi.events.RecordingEventListener;
import com.zutubi.pulse.core.test.api.PulseTestCase;
import com.zutubi.pulse.master.events.AgentStatusChangeEvent;
import com.zutubi.pulse.master.events.AgentSynchronisationCompleteEvent;
import com.zutubi.pulse.master.events.AgentSynchronisationMessageProcessedEvent;
import com.zutubi.pulse.master.model.AgentState;
import com.zutubi.pulse.master.model.AgentSynchronisationMessage;
import com.zutubi.pulse.master.model.HostState;
import com.zutubi.pulse.master.model.persistence.AgentStateDao;
import com.zutubi.pulse.master.model.persistence.AgentSynchronisationMessageDao;
import com.zutubi.pulse.master.scheduling.CallbackService;
import com.zutubi.pulse.master.security.PulseThreadFactory;
import com.zutubi.pulse.master.tove.config.agent.AgentConfiguration;
import com.zutubi.pulse.servercore.agent.DeleteDirectoryTask;
import com.zutubi.pulse.servercore.agent.SynchronisationMessage;
import com.zutubi.pulse.servercore.agent.SynchronisationMessageResult;
import com.zutubi.pulse.servercore.agent.SynchronisationTaskFactory;
import com.zutubi.util.*;
import com.zutubi.util.bean.DefaultObjectFactory;
import org.mockito.InOrder;
import org.mockito.Matchers;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.*;

public class AgentSynchronisationServiceTest extends PulseTestCase
{
    private static final long EVENT_TIMEOUT = 30 * Constants.SECOND;

    private List<SynchronisationMessageResult> results = new LinkedList<SynchronisationMessageResult>();
    private EventManager eventManager;
    private RecordingEventListener listener;
    private AtomicLong nextId = new AtomicLong(1);
    private AgentState agentState;
    private AgentService agentService;
    private DefaultAgent agent;
    private AgentSynchronisationMessageDao messageDao;
    private Semaphore serviceSyncFlag = new Semaphore(0);
    private SynchronisationTaskFactory synchronisationTaskFactory;
    private AgentSynchronisationService agentSynchronisationService;
    private DefaultAgentManager agentManager;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();

        eventManager = new DefaultEventManager();
        listener = new RecordingEventListener(AgentSynchronisationCompleteEvent.class);
        eventManager.register(listener);

        agentState = new AgentState();
        agentState.setId(nextId.getAndIncrement());

        agentService = mock(AgentService.class);
        doAnswer(new Answer<List<SynchronisationMessageResult>>()
        {
            public List<SynchronisationMessageResult> answer(InvocationOnMock invocationOnMock) throws Throwable
            {
                serviceSyncFlag.release();
                return results;
            }
        }).when(agentService).synchronise(Matchers.<List<SynchronisationMessage>>anyObject());

        agent = new DefaultAgent(new AgentConfiguration("test"), agentState, agentService, new DefaultHost(new HostState()));

        AgentStateDao agentStateDao = mock(AgentStateDao.class);
        doReturn(agentState).when(agentStateDao).findById(anyLong());

        messageDao = new InMemoryAgentSynchronisationMessageDao();
        
        AgentStatusManager agentStatusManager = mock(AgentStatusManager.class);
        doAnswer(new Answer<Object>()
        {
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable
            {
                @SuppressWarnings({"unchecked"})
                NullaryFunction<Object> fn = (NullaryFunction<Object>) invocationOnMock.getArguments()[0];
                return fn.process();
            }
        }).when(agentStatusManager).withAgentsLock(Matchers.<NullaryFunction<Object>>anyObject());

        agentManager = new DefaultAgentManager()
        {
            @Override
            public Agent getAgentByHandle(long handle)
            {
                return agent;
            }
        };
        
        agentManager.setAgentStateDao(agentStateDao);
        agentManager.setAgentSynchronisationMessageDao(messageDao);
        agentManager.setEventManager(eventManager);
        agentManager.setAgentStatusManager(agentStatusManager);
        
        synchronisationTaskFactory = new SynchronisationTaskFactory();
        synchronisationTaskFactory.setObjectFactory(new DefaultObjectFactory());

        agentSynchronisationService = new AgentSynchronisationService();
        agentSynchronisationService.setEventManager(eventManager);
        agentSynchronisationService.setThreadFactory(new PulseThreadFactory());
        agentSynchronisationService.setCallbackService(mock(CallbackService.class));
        agentManager.setAgentSynchronisationService(agentSynchronisationService);
        agentSynchronisationService.init(agentManager);
    }

    public void testNoMessages()
    {
        publishStatusChange();
        
        AgentSynchronisationCompleteEvent event = awaitEvent();
        assertEquals(agent, event.getAgent());
        assertTrue(event.isSuccessful());
        verifyNoMoreInteractions(agentService);
    }

    public void testSingleSuccessfulMessage()
    {
        AgentSynchronisationMessage message = enqueueSuccessfulMessage();

        publishStatusChange();

        awaitEvent();
        assertEquals(1, messageDao.count());
        AgentSynchronisationMessage savedMessage = messageDao.findAll().get(0);
        assertNotSame(message, savedMessage);
        assertEquals(AgentSynchronisationMessage.Status.SUCCEEDED, savedMessage.getStatus());
        assertNull(savedMessage.getStatusMessage());
    }

    public void testSingleFailedMessage()
    {
        final String STATUS_MESSAGE = "it failed";

        enqueueFailedMessage(STATUS_MESSAGE);

        publishStatusChange();

        awaitEvent();
        assertEquals(1, messageDao.count());
        AgentSynchronisationMessage savedMessage =  messageDao.findAll().get(0);
        assertEquals(AgentSynchronisationMessage.Status.FAILED_PERMANENTLY, savedMessage.getStatus());
        assertEquals(STATUS_MESSAGE, savedMessage.getStatusMessage());
    }

    public void testMultipleMessages()
    {
        enqueueSuccessfulMessage();
        enqueueSuccessfulMessage();
        enqueueSuccessfulMessage();

        publishStatusChange();

        awaitEvent();
        assertEquals(3, messageDao.count());
        for (AgentSynchronisationMessage message: messageDao.findAll())
        {
            assertEquals(AgentSynchronisationMessage.Status.SUCCEEDED, message.getStatus());
        }
    }

    public void testCompleteMessagesNotResent()
    {
        enqueueSuccessfulMessage();
        enqueueFailedMessage("anything");
        publishStatusChange();
        awaitEvent();
        verify(agentService).synchronise(Matchers.<List<SynchronisationMessage>>anyObject());
        results.clear();

        publishStatusChange();

        awaitEvent();
        verifyNoMoreInteractions(agentService);
    }

    public void testMixOfCompleteAndNew()
    {
        InOrder inOrder = inOrder(agentService);

        AgentSynchronisationMessage message1 = enqueueSuccessfulMessage();
        publishStatusChange();
        awaitEvent();
        inOrder.verify(agentService).synchronise(asList(getMessageThatWouldBeSent(message1)));
        results.clear();

        AgentSynchronisationMessage message2 = enqueueSuccessfulMessage();

        publishStatusChange();

        awaitEvent();
        inOrder.verify(agentService).synchronise(asList(getMessageThatWouldBeSent(message2)));
    }

    public void testSendingFailure()
    {
        doThrow(new RuntimeException("badness")).when(agentService).synchronise(Matchers.<List<SynchronisationMessage>>anyObject());
        enqueueSuccessfulMessage();

        publishStatusChange();
        
        awaitEvent();
        assertEquals(1, messageDao.count());
        AgentSynchronisationMessage message =  messageDao.findAll().get(0);
        assertEquals(AgentSynchronisationMessage.Status.SENDING_FAILED, message.getStatus());
        assertThat(message.getStatusMessage(), containsString("badness"));
    }

    public void testSendingFailureMessageRetried()
    {
        AgentSynchronisationMessage message = enqueueSuccessfulMessage();
        message.setStatus(AgentSynchronisationMessage.Status.SENDING_FAILED);
        message.setStatusMessage("it send did failed");

        publishStatusChange();

        awaitEvent();
        assertEquals(1, messageDao.count());
        AgentSynchronisationMessage savedMessage =  messageDao.findAll().get(0);
        assertEquals(AgentSynchronisationMessage.Status.SUCCEEDED, savedMessage.getStatus());
        assertNull(savedMessage.getStatusMessage());
    }

    public void testOldCompleteMessagesCleaned()
    {
        long firstId = nextId.get();
        for (int i = 0; i < 12; i++)
        {
            enqueueSuccessfulMessage();
        }

        publishStatusChange();

        awaitEvent();
        assertEquals(10, messageDao.count());
        assertEquals(firstId + 2,  messageDao.findAll().get(0).getId());
    }

    public void testCompletionFails()
    {
        final boolean[] first = new boolean[]{true};
        doAnswer(new Answer<List<SynchronisationMessageResult>>()
        {
            public List<SynchronisationMessageResult> answer(InvocationOnMock invocationOnMock) throws Throwable
            {
                serviceSyncFlag.release();

                // First time through, add a new message.
                if (first[0])
                {
                    first[0] = false;
                    enqueueSuccessfulMessage();
                }
                
                return results;
            }
        }).when(agentService).synchronise(Matchers.<List<SynchronisationMessage>>anyObject());

        enqueueSuccessfulMessage();

        publishStatusChange();

        awaitEvent();
        List<AgentSynchronisationMessage> messages = messageDao.findAll();
        assertEquals(2, messages.size());
        AgentSynchronisationMessage savedMessage = messages.get(0);
        assertEquals(AgentSynchronisationMessage.Status.SUCCEEDED, savedMessage.getStatus());
        savedMessage = messages.get(1);
        assertEquals(AgentSynchronisationMessage.Status.SUCCEEDED, savedMessage.getStatus());
    }
    
    public void testAsynchronousMessage() throws InterruptedException
    {
        AgentSynchronisationMessage message = enqueueNewMessage();
        publishStatusChange();
        
        awaitServiceSync();
        
        assertEquals(0, listener.getReceivedCount());
        assertEquals(1, messageDao.count());
        message = messageDao.findById(message.getId());
        assertEquals(AgentSynchronisationMessage.Status.PROCESSING, message.getStatus());

        publishSuccessfulResult(message.getId());
        
        AgentSynchronisationCompleteEvent synchronisationCompleteEvent = awaitEvent();
        assertTrue(synchronisationCompleteEvent.isSuccessful());
        assertEquals(1, messageDao.count());
        message = messageDao.findById(message.getId());
        assertEquals(AgentSynchronisationMessage.Status.SUCCEEDED, message.getStatus());
    }

    public void testAsynchronousMessageFailure() throws InterruptedException
    {
        AgentSynchronisationMessage message = enqueueNewMessage();
        publishStatusChange();
        
        awaitServiceSync();
        
        assertEquals(0, listener.getReceivedCount());
        assertEquals(1, messageDao.count());
        message = messageDao.findById(message.getId());
        assertEquals(AgentSynchronisationMessage.Status.PROCESSING, message.getStatus());

        publishFailedResult(message.getId());
        
        AgentSynchronisationCompleteEvent synchronisationCompleteEvent = awaitEvent();
        assertTrue(synchronisationCompleteEvent.isSuccessful());
        assertEquals(1, messageDao.count());
        message = messageDao.findById(message.getId());
        assertEquals(AgentSynchronisationMessage.Status.FAILED_PERMANENTLY, message.getStatus());
    }

    public void testMultipleAsynchronousMessages() throws InterruptedException
    {
        AgentSynchronisationMessage message1 = enqueueNewMessage();
        AgentSynchronisationMessage message2 = enqueueNewMessage();
        AgentSynchronisationMessage message3 = enqueueNewMessage();
        
        publishStatusChange();
        
        awaitServiceSync();
        
        assertEquals(0, listener.getReceivedCount());
        assertEquals(3, messageDao.count());
        for (AgentSynchronisationMessage message: messageDao.findAll())
        {
            assertEquals(AgentSynchronisationMessage.Status.PROCESSING, message.getStatus());
        }

        publishSuccessfulResult(message1.getId());
        publishSuccessfulResult(message3.getId());

        Thread.sleep(500);
        
        assertEquals(0, listener.getReceivedCount());

        publishSuccessfulResult(message2.getId());
        
        AgentSynchronisationCompleteEvent synchronisationCompleteEvent = awaitEvent();
        assertTrue(synchronisationCompleteEvent.isSuccessful());
        assertEquals(3, messageDao.count());
        for (AgentSynchronisationMessage message: messageDao.findAll())
        {
            assertEquals(AgentSynchronisationMessage.Status.SUCCEEDED, message.getStatus());
        }
    }

    public void testSynchronousAndAsynchronousMessages() throws InterruptedException
    {
        AgentSynchronisationMessage asyncMessage = enqueueNewMessage();
        enqueueSuccessfulMessage();
        
        publishStatusChange();
        
        awaitServiceSync();
        
        assertEquals(0, listener.getReceivedCount());

        publishSuccessfulResult(asyncMessage.getId());
        
        AgentSynchronisationCompleteEvent synchronisationCompleteEvent = awaitEvent();
        assertTrue(synchronisationCompleteEvent.isSuccessful());
        assertEquals(2, messageDao.count());
        for (AgentSynchronisationMessage message: messageDao.findAll())
        {
            assertEquals(AgentSynchronisationMessage.Status.SUCCEEDED, message.getStatus());
        }
    }

    public void testAsynchronousMessageTimeout() throws InterruptedException
    {
        TestClock clock = new TestClock(0);
        agentSynchronisationService.setClock(clock);
        
        AgentSynchronisationMessage message = enqueueNewMessage();
        
        publishStatusChange();
        
        awaitServiceSync();
        
        assertEquals(0, listener.getReceivedCount());
        clock.setTime(Long.MAX_VALUE);
        agentSynchronisationService.checkTimeouts();
        
        AgentSynchronisationCompleteEvent synchronisationCompleteEvent = awaitEvent();
        assertTrue(synchronisationCompleteEvent.isSuccessful());
        assertEquals(AgentSynchronisationMessage.Status.TIMED_OUT, messageDao.findById(message.getId()).getStatus());
    }

    public void testRestartWhileAsynchronousMessageProcessing() throws InterruptedException
    {
        AgentSynchronisationMessage message = enqueueNewMessage();
        
        publishStatusChange();
        
        awaitServiceSync();

        agentSynchronisationService.init(agentManager);
        
        assertEquals(0, listener.getReceivedCount());
        assertEquals(AgentSynchronisationMessage.Status.SENDING_FAILED, messageDao.findById(message.getId()).getStatus());
    }
    
    private AgentSynchronisationMessage enqueueSuccessfulMessage()
    {
        AgentSynchronisationMessage message = enqueueNewMessage();
        results.add(new SynchronisationMessageResult(message.getId()));
        return message;
    }

    private void enqueueFailedMessage(String statusMessage)
    {
        AgentSynchronisationMessage message = enqueueNewMessage();
        results.add(new SynchronisationMessageResult(message.getId(), false, statusMessage));
    }

    private AgentSynchronisationMessage enqueueNewMessage()
    {
        SynchronisationMessage message = synchronisationTaskFactory.toMessage(new DeleteDirectoryTask("blah", "baz", Collections.<String, String>emptyMap()));
        AgentSynchronisationMessage agentMessage = new AgentSynchronisationMessage(agentState, message, "desc");
        agentMessage.setId(nextId.getAndIncrement());
        messageDao.save(agentMessage);
        return agentMessage;
    }

    private SynchronisationMessage getMessageThatWouldBeSent(AgentSynchronisationMessage agentSynchronisationMessage)
    {
        SynchronisationMessage message = agentSynchronisationMessage.getMessage();
        message.setId(agentSynchronisationMessage.getId());
        return message;
    }
    
    private void publishStatusChange()
    {
        eventManager.publish(new AgentStatusChangeEvent(this, agent, AgentStatus.OFFLINE, AgentStatus.SYNCHRONISING));
    }

    private void publishSuccessfulResult(long messageId)
    {
        eventManager.publish(new AgentSynchronisationMessageProcessedEvent(this, agent, new SynchronisationMessageResult(messageId)));
    }
    
    private void publishFailedResult(long messageId)
    {
        eventManager.publish(new AgentSynchronisationMessageProcessedEvent(this, agent, new SynchronisationMessageResult(messageId, false, "nasty")));
    }
    
    private AgentSynchronisationCompleteEvent awaitEvent()
    {
        long startTime = System.currentTimeMillis();
        while (listener.getEventsReceived().size() == 0)
        {
            if (System.currentTimeMillis() - startTime > EVENT_TIMEOUT)
            {
                fail("Timed out waiting for event.");
            }

            try
            {
                Thread.sleep(25);
            }
            catch (InterruptedException e)
            {
                // noop
            }
        }

        return (AgentSynchronisationCompleteEvent) listener.getEventsReceived().remove(0);
    }

    private void awaitServiceSync()
    {
        try
        {
            serviceSyncFlag.tryAcquire(30, TimeUnit.SECONDS);
        }
        catch (InterruptedException e)
        {
            fail("Timed out waiting for sync call on agent service");
        }
    }
    
    private static class InMemoryAgentSynchronisationMessageDao implements AgentSynchronisationMessageDao
    {
        private List<AgentSynchronisationMessage> messages = new LinkedList<AgentSynchronisationMessage>();
        
        public AgentSynchronisationMessage findById(final long id)
        {
            return copy(CollectionUtils.find(messages, new Predicate<AgentSynchronisationMessage>()
            {
                public boolean satisfied(AgentSynchronisationMessage agentSynchronisationMessage)
                {
                    return agentSynchronisationMessage.getId() == id;
                }
            }));
        }

        public void flush()
        {
        }

        public List<AgentSynchronisationMessage> findAll()
        {
            return copy(messages);
        }

        public void save(AgentSynchronisationMessage entity)
        {
            int index = messages.indexOf(entity);
            if (index < 0)
            {
                messages.add(entity);
            }
            else
            {
                messages.remove(index);
                messages.add(index, entity);
            }
        }

        public void delete(AgentSynchronisationMessage entity)
        {
            messages.remove(entity);
        }

        public void refresh(AgentSynchronisationMessage entity)
        {
        }

        public int count()
        {
            return messages.size();
        }

        public List<AgentSynchronisationMessage> findByAgentState(AgentState agentState)
        {
            return findAll();
        }

        public List<AgentSynchronisationMessage> findByStatus(final AgentSynchronisationMessage.Status status)
        {
            return copy(CollectionUtils.filter(messages, new Predicate<AgentSynchronisationMessage>()
            {
                public boolean satisfied(AgentSynchronisationMessage agentSynchronisationMessage)
                {
                    return agentSynchronisationMessage.getStatus() == status;
                }
            }));
        }

        public int deleteByAgentState(AgentState agentState)
        {
            int count = messages.size();
            messages.clear();
            return count;
        }

        private List<AgentSynchronisationMessage> copy(List<AgentSynchronisationMessage> messages)
        {
            List<AgentSynchronisationMessage> copy = new LinkedList<AgentSynchronisationMessage>();
            for (AgentSynchronisationMessage message: messages)
            {
                copy.add(copy(message));
            }

            return copy;
        }

        private AgentSynchronisationMessage copy(AgentSynchronisationMessage message)
        {
            if (message == null)
            {
                return null;
            }
        
            AgentSynchronisationMessage copy = new AgentSynchronisationMessage(message.getAgentState(), copy(message.getMessage()), message.getDescription());
            copy.setId(message.getId());
            copy.setStatus(message.getStatus());
            copy.setProcessingTimestamp(message.getProcessingTimestamp());
            copy.setStatusMessage(message.getStatusMessage());
            return copy;
        }

        private SynchronisationMessage copy(SynchronisationMessage message)
        {
            Properties arguments = new Properties();
            arguments.putAll(message.getArguments());
            return new SynchronisationMessage(message.getTypeName(), arguments);
        }
    }    
}
