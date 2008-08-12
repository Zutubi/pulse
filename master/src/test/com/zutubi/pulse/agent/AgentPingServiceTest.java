package com.zutubi.pulse.agent;

import com.zutubi.pulse.AgentService;
import com.zutubi.pulse.SystemInfo;
import com.zutubi.pulse.Version;
import com.zutubi.pulse.core.RecipeRequest;
import com.zutubi.pulse.core.config.Resource;
import com.zutubi.pulse.events.*;
import com.zutubi.pulse.logging.CustomLogRecord;
import com.zutubi.pulse.model.ResourceRequirement;
import com.zutubi.pulse.security.PulseThreadFactory;
import com.zutubi.pulse.services.SlaveStatus;
import com.zutubi.pulse.test.PulseTestCase;
import com.zutubi.pulse.tove.config.agent.AgentConfiguration;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.stub;

import java.io.File;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 */
public class AgentPingServiceTest extends PulseTestCase
{
    private static final String TEST_MASTER = "master.location";
    private static final String TEST_MASTER_URL = "http://" + TEST_MASTER;
    private static final int BUILD_NUMBER = Version.getVersion().getBuildNumberAsInt();

    private BlockingQueue<AgentPingEvent> events = new LinkedBlockingQueue<AgentPingEvent>();
    private AgentPingService agentPingService;

    protected void setUp() throws Exception
    {
        super.setUp();

        EventManager eventManager = new DefaultEventManager();
        eventManager.register(new EventListener()
        {
            public void handleEvent(Event evt)
            {
                events.add((AgentPingEvent) evt);
            }

            public Class[] getHandledEvents()
            {
                return new Class[]{AgentPingEvent.class};
            }
        });

        agentPingService = new AgentPingService();
        agentPingService.setEventManager(eventManager);
        agentPingService.setThreadFactory(new PulseThreadFactory());
        agentPingService.setMasterLocationProvider(new MasterLocationProvider()
        {
            public String getMasterLocation()
            {
                return TEST_MASTER;
            }

            public String getMasterUrl()
            {
                return TEST_MASTER_URL;
            }
        });

        agentPingService.init();
    }

    public void testSimplePing()
    {
        Agent agent = createAgent(1);
        AgentService slaveService = createAgentService();

        agentPingService.requestPing(agent, slaveService);
        assertEvent(agent);
        assertNoMoreEvents();
    }

    public void testMultiplePings()
    {
        Agent agent1 = createAgent(1);
        Agent agent2 = createAgent(2);
        Agent agent3 = createAgent(3);

        AgentService service = createAgentService();

        agentPingService.requestPing(agent1, service);
        assertEvent(agent1);
        agentPingService.requestPing(agent2, service);
        assertEvent(agent2);
        agentPingService.requestPing(agent3, service);
        assertEvent(agent3);

        assertNoMoreEvents();
    }

    public void testMultiplePingsSameAgent()
    {
        Agent agent = createAgent(1);
        AgentService service = createAgentService();

        agentPingService.requestPing(agent, service);
        assertEvent(agent);
        agentPingService.requestPing(agent, service);
        assertEvent(agent);
        agentPingService.requestPing(agent, service);
        assertEvent(agent);

        assertNoMoreEvents();
    }

    public void testDuplicateIgnored()
    {
        Agent agent = createAgent(1);

        WaitingAgentService service = new WaitingAgentService();

        agentPingService.requestPing(agent, service);
        agentPingService.requestPing(agent, service);

        service.release();
        service.release();

        assertEvent(agent);
        assertNoMoreEvents();
    }

    public void testTimeout() throws InterruptedException
    {
        System.setProperty(AgentPingService.PROPERTY_AGENT_PING_TIMEOUT, "1");

        Agent agent = createAgent(1);

        WaitingAgentService service = new WaitingAgentService();

        agentPingService.requestPing(agent, service);
        Thread.sleep(2000);
        service.release();

        assertEvent(agent, PingStatus.OFFLINE, "Agent ping timed out");
        assertNoMoreEvents();
    }

    private void assertEvent(Agent agent)
    {
        assertEvent(agent, PingStatus.IDLE, null);
    }

    private void assertEvent(Agent agent, PingStatus status, String message)
    {
        try
        {
            AgentPingEvent event = events.poll(5, TimeUnit.SECONDS);
            assertNotNull(event);
            assertEquals(agent.getId(), event.getAgent().getId());
            assertEquals(status, event.getPingStatus().getStatus());
            if(message != null)
            {
                assertEquals(message,event.getPingStatus().getMessage());
            }
        }
        catch (InterruptedException e)
        {
            throw new RuntimeException(e);
        }
    }

    private void assertNoMoreEvents()
    {
        assertNull(events.peek());
    }

    private AgentService createAgentService()
    {
        AgentService mockService = mock(AgentService.class);
        stub(mockService.ping()).toReturn(BUILD_NUMBER);
        stub(mockService.getStatus(TEST_MASTER_URL)).toReturn(new SlaveStatus(PingStatus.IDLE));
        return mockService;
    }

    private Agent createAgent(long id)
    {
        Agent mockAgent = mock(Agent.class);
        stub(mockAgent.getId()).toReturn(id);
        stub(mockAgent.getConfig()).toReturn(getConfig());
        return mockAgent;
    }

    private AgentConfiguration getConfig()
    {
        AgentConfiguration config = new AgentConfiguration();
        config.setName("test");
        return config;
    }

    private class WaitingAgentService implements AgentService
    {
        private Semaphore flag = new Semaphore(0);

        public void release()
        {
            flag.release();
        }

        public int ping()
        {
            try
            {
                flag.acquire();
            }
            catch (InterruptedException e)
            {
                throw new RuntimeException(e);
            }

            return BUILD_NUMBER;
        }

        public SlaveStatus getStatus(String masterLocation)
        {
            return new SlaveStatus(PingStatus.IDLE);
        }

        public boolean updateVersion(String masterBuild, String masterUrl, long handle, String packageUrl, long packageSize)
        {
            throw new RuntimeException("Not implemented");
        }

        public List<Resource> discoverResources()
        {
            throw new RuntimeException("Not implemented");
        }

        public SystemInfo getSystemInfo()
        {
            throw new RuntimeException("Not implemented");
        }

        public List<CustomLogRecord> getRecentMessages()
        {
            throw new RuntimeException("Not implemented");
        }

        public AgentConfiguration getAgentConfig()
        {
            return getConfig();
        }

        public boolean hasResource(ResourceRequirement requirement)
        {
            throw new RuntimeException("Not implemented");
        }

        public boolean build(RecipeRequest request)
        {
            throw new RuntimeException("Not implemented");
        }

        public void collectResults(String project, long recipeId, boolean incremental, File outputDest, File workDest)
        {
            throw new RuntimeException("Not implemented");
        }

        public void cleanup(String project, long recipeId, boolean incremental)
        {
            throw new RuntimeException("Not implemented");
        }

        public void terminateRecipe(long recipeId)
        {
            throw new RuntimeException("Not implemented");
        }

        public String getHostName()
        {
            throw new RuntimeException("Not implemented");
        }

        public void garbageCollect()
        {
            throw new RuntimeException("Not implemented");
        }

        public String getUrl()
        {
            throw new RuntimeException("Not implemented");
        }
    }
}
