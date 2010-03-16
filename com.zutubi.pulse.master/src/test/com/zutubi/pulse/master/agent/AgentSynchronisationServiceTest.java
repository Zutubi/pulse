package com.zutubi.pulse.master.agent;

import com.zutubi.events.DefaultEventManager;
import com.zutubi.events.EventManager;
import com.zutubi.events.RecordingEventListener;
import com.zutubi.pulse.core.test.api.PulseTestCase;
import com.zutubi.pulse.master.events.AgentStatusChangeEvent;
import com.zutubi.pulse.master.events.AgentSynchronisationCompleteEvent;
import com.zutubi.pulse.master.model.AgentState;
import com.zutubi.pulse.master.model.AgentSynchronisationMessage;
import com.zutubi.pulse.master.model.HostState;
import com.zutubi.pulse.master.security.PulseThreadFactory;
import com.zutubi.pulse.master.tove.config.agent.AgentConfiguration;
import com.zutubi.pulse.servercore.agent.DeleteDirectoryTask;
import com.zutubi.pulse.servercore.agent.SynchronisationMessage;
import com.zutubi.pulse.servercore.agent.SynchronisationMessageResult;
import com.zutubi.util.Constants;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import org.mockito.InOrder;
import org.mockito.Matchers;
import static org.mockito.Mockito.*;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import static java.util.Arrays.asList;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicLong;

public class AgentSynchronisationServiceTest extends PulseTestCase
{
    private static final long EVENT_TIMEOUT = 30 * Constants.SECOND;

    private List<AgentSynchronisationMessage> messages = new LinkedList<AgentSynchronisationMessage>();
    private List<SynchronisationMessageResult> results = new LinkedList<SynchronisationMessageResult>();
    private EventManager eventManager;
    private RecordingEventListener listener;
    private AtomicLong nextId = new AtomicLong(1);
    private AgentState agentState;
    private AgentService agentService;
    private DefaultAgent agent;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();

        agentState = new AgentState();
        agentState.setId(nextId.getAndIncrement());

        agentService = mock(AgentService.class);
        doReturn(results).when(agentService).synchronise(Matchers.<List<SynchronisationMessage>>anyObject());

        agent = new DefaultAgent(new AgentConfiguration("test"), agentState, agentService, new DefaultHost(new HostState()));

        AgentManager agentManager = mock(AgentManager.class);
        doAnswer(new Answer<List<AgentSynchronisationMessage>>()
        {
            public List<AgentSynchronisationMessage> answer(InvocationOnMock invocationOnMock) throws Throwable
            {
                // Return a copy so we force calls to save to update our list.
                return copy(messages);
            }
        }).when(agentManager).getSynchronisationMessages(Matchers.<Agent>anyObject());

        doAnswer(new Answer<Object>()
        {
            @SuppressWarnings({"unchecked"})
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable
            {
                List<AgentSynchronisationMessage> toSave = (List<AgentSynchronisationMessage>) invocationOnMock.getArguments()[0];
                for (AgentSynchronisationMessage message: toSave)
                {
                    int index = messages.indexOf(message);
                    messages.remove(index);
                    messages.add(index, message);
                }
                return null;
            }
        }).when(agentManager).saveSynchronisationMessages(Matchers.<List<AgentSynchronisationMessage>>anyObject());

        doAnswer(new Answer()
        {
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable
            {
                AgentSynchronisationMessage message = (AgentSynchronisationMessage) invocationOnMock.getArguments()[0];
                messages.remove(message);
                return null;
            }
        }).when(agentManager).dequeueSynchronisationMessage(Matchers.<AgentSynchronisationMessage>anyObject());

        eventManager = new DefaultEventManager();
        listener = new RecordingEventListener(AgentSynchronisationCompleteEvent.class);
        eventManager.register(listener);

        AgentSynchronisationService service = new AgentSynchronisationService();
        service.setAgentManager(agentManager);
        service.setEventManager(eventManager);
        service.setThreadFactory(new PulseThreadFactory());
        service.init();
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
        AgentSynchronisationMessage copy = new AgentSynchronisationMessage(message.getAgentState(), copy(message.getMessage()), message.getDescription());
        copy.setId(message.getId());
        copy.setStatus(message.getStatus());
        copy.setStatusMessage(message.getStatusMessage());
        return copy;
    }

    private SynchronisationMessage copy(SynchronisationMessage message)
    {
        Properties arguments = new Properties();
        arguments.putAll(message.getArguments());
        return new SynchronisationMessage(message.getType(), arguments);
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
        assertEquals(1, messages.size());
        AgentSynchronisationMessage savedMessage = messages.get(0);
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
        assertEquals(1, messages.size());
        AgentSynchronisationMessage savedMessage = messages.get(0);
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
        assertEquals(3, messages.size());
        for (AgentSynchronisationMessage message: messages)
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
        inOrder.verify(agentService).synchronise(asList(message1.getMessage()));
        results.clear();

        AgentSynchronisationMessage message2 = enqueueSuccessfulMessage();

        publishStatusChange();

        awaitEvent();
        inOrder.verify(agentService).synchronise(asList(message2.getMessage()));
    }

    public void testSendingFailure()
    {
        doThrow(new RuntimeException("badness")).when(agentService).synchronise(Matchers.<List<SynchronisationMessage>>anyObject());
        enqueueSuccessfulMessage();

        publishStatusChange();
        
        awaitEvent();
        assertEquals(1, messages.size());
        AgentSynchronisationMessage message = messages.get(0);
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
        assertEquals(1, messages.size());
        AgentSynchronisationMessage savedMessage = messages.get(0);
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
        assertEquals(10, messages.size());
        assertEquals(firstId + 2, messages.get(0).getId());
    }

    private AgentSynchronisationMessage enqueueSuccessfulMessage()
    {
        AgentSynchronisationMessage message = new AgentSynchronisationMessage(agentState, new DeleteDirectoryTask("blah").toMessage(), "desc");
        message.setId(nextId.getAndIncrement());
        messages.add(message);
        results.add(new SynchronisationMessageResult());
        return message;
    }

    private void enqueueFailedMessage(String statusMessage)
    {
        AgentSynchronisationMessage message = new AgentSynchronisationMessage(agentState, new DeleteDirectoryTask("blah").toMessage(), "desc");
        message.setId(nextId.getAndIncrement());
        messages.add(message);
        results.add(new SynchronisationMessageResult(false, statusMessage));
    }

    private void publishStatusChange()
    {
        eventManager.publish(new AgentStatusChangeEvent(this, agent, AgentStatus.OFFLINE, AgentStatus.SYNCHRONISING));
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
}
