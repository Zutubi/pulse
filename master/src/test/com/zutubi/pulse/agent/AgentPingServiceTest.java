package com.zutubi.pulse.agent;

import com.zutubi.pulse.events.*;
import com.zutubi.pulse.services.SlaveService;
import com.zutubi.pulse.services.SlaveStatus;
import com.zutubi.pulse.services.TokenManager;
import com.zutubi.pulse.services.InvalidTokenException;
import com.zutubi.pulse.test.PulseTestCase;
import com.zutubi.pulse.core.RecipeRequest;
import com.zutubi.pulse.core.model.Resource;
import com.zutubi.pulse.BuildContext;
import com.zutubi.pulse.SystemInfo;
import com.zutubi.pulse.Version;
import com.zutubi.pulse.resources.ResourceConstructor;
import com.zutubi.pulse.filesystem.FileInfo;
import com.zutubi.pulse.logging.CustomLogRecord;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.stub;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.Semaphore;
import java.util.List;

/**
 */
public class AgentPingServiceTest extends PulseTestCase
{
    private static final String TEST_TOKEN = "test token";
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

        agentPingService.setServiceTokenManager(new TokenManager()
        {
            public String getToken()
            {
                return TEST_TOKEN;
            }
        });

        agentPingService.init();
    }

    public void testSimplePing()
    {
        Agent agent = createAgent(1);
        SlaveService slaveService = createSlaveService();

        agentPingService.requestPing(agent, slaveService);
        assertEvent(agent);
        assertNoMoreEvents();
    }

    public void testMultiplePings()
    {
        Agent agent1 = createAgent(1);
        Agent agent2 = createAgent(2);
        Agent agent3 = createAgent(3);

        SlaveService service = createSlaveService();

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
        SlaveService service = createSlaveService();

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
        
        WaitingSlaveService service = new WaitingSlaveService();

        agentPingService.requestPing(agent, service);
        agentPingService.requestPing(agent, service);

        service.release();

        assertEvent(agent);
        assertNoMoreEvents();
    }

    public void testTimeout() throws InterruptedException
    {
        System.setProperty(AgentPingService.PPROPERTY_AGENT_PING_TIMEOUT, "1");

        Agent agent = createAgent(1);

        WaitingSlaveService service = new WaitingSlaveService();

        agentPingService.requestPing(agent, service);
        Thread.sleep(2000);
        service.release();

        assertEvent(agent, Status.OFFLINE, "Agent ping timed out");
        assertNoMoreEvents();
    }

    private void assertEvent(Agent agent)
    {
        assertEvent(agent, Status.IDLE, null);
    }

    private void assertEvent(Agent agent, Status status, String message)
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

    private SlaveService createSlaveService()
    {
        SlaveService mockService = mock(SlaveService.class);
        stub(mockService.ping()).toReturn(BUILD_NUMBER);
        stub(mockService.getStatus(TEST_TOKEN, TEST_MASTER_URL)).toReturn(new SlaveStatus(Status.IDLE));
        return mockService;
    }

    private Agent createAgent(long id)
    {
        Agent mockAgent = mock(Agent.class);
        stub(mockAgent.getId()).toReturn(id);
        stub(mockAgent.getName()).toReturn("test");
        return mockAgent;
    }

    private class WaitingSlaveService implements SlaveService
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

        public SlaveStatus getStatus(String token, String master)
        {
            return new SlaveStatus(Status.IDLE);
        }

        public boolean updateVersion(String token, String build, String master, long id, String packageUrl, long packageSize)
        {
            throw new RuntimeException("Not implemented");
        }

        public boolean build(String token, String master, long slaveId, RecipeRequest request, BuildContext context) throws InvalidTokenException
        {
            throw new RuntimeException("Not implemented");
        }

        public void cleanupRecipe(String token, String project, String spec, long recipeId, boolean incremental) throws InvalidTokenException
        {
            throw new RuntimeException("Not implemented");
        }

        public void terminateRecipe(String token, long recipeId) throws InvalidTokenException
        {
            throw new RuntimeException("Not implemented");
        }

        public SystemInfo getSystemInfo(String token) throws InvalidTokenException
        {
            throw new RuntimeException("Not implemented");
        }

        public List<CustomLogRecord> getRecentMessages(String token) throws InvalidTokenException
        {
            throw new RuntimeException("Not implemented");
        }

        public List<Resource> discoverResources(String token)
        {
            throw new RuntimeException("Not implemented");
        }

        public FileInfo getFileInfo(String token, String path)
        {
            throw new RuntimeException("Not implemented");
        }

        public String[] listRoots(String token)
        {
            throw new RuntimeException("Not implemented");
        }

        public Resource createResource(ResourceConstructor constructor, String path)
        {
            throw new RuntimeException("Not implemented");
        }

        public boolean isResourceHome(ResourceConstructor constructor, String path)
        {
            throw new RuntimeException("Not implemented");
        }
    }
}
