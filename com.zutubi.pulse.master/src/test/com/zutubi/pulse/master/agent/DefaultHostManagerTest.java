package com.zutubi.pulse.master.agent;

import com.google.common.base.Predicate;
import static com.google.common.collect.Iterables.any;
import com.zutubi.pulse.core.test.api.PulseTestCase;
import com.zutubi.pulse.master.model.AgentState;
import com.zutubi.pulse.master.model.HostState;
import com.zutubi.pulse.master.model.persistence.InMemoryEntityDao;
import com.zutubi.pulse.master.tove.config.agent.AgentConfiguration;
import com.zutubi.util.bean.DefaultObjectFactory;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.stub;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class DefaultHostManagerTest extends PulseTestCase
{
    private static final String TEST_NAME_1 = "agent1";
    private static final String TEST_NAME_2 = "agent2";
    private static final String TEST_HOST_1 = "host1";
    private static final String TEST_HOST_2 = "host2";
    private static final int TEST_PORT = 2020;

    private AgentManager agentManager;
    private DefaultHostManager hostManager;
    private InMemoryEntityDao<HostState> hostStateDao;
    private long nextHandle = 1;
    private Map<Long, Agent> agentsByHandle = new HashMap<Long, Agent>();

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();

        hostManager = new DefaultHostManager();
        hostStateDao = new InMemoryEntityDao<HostState>();
        hostManager.setHostStateDao(hostStateDao);
        hostManager.setHostPingService(mock(HostPingService.class));
        hostManager.setObjectFactory(new DefaultObjectFactory());
        hostManager.setSlaveProxyFactory(mock(SlaveProxyFactory.class));

        agentManager = mock(AgentManager.class);
        stub(agentManager.getAgentByHandle(anyLong())).toAnswer(new Answer<Agent>()
        {
            public Agent answer(InvocationOnMock invocationOnMock) throws Throwable
            {
                Long handle = (Long) invocationOnMock.getArguments()[0];
                return agentsByHandle.get(handle);
            }
        });

        hostManager.init(agentManager);
    }

    public void testAgentAdded()
    {
        AgentConfiguration config = createRemoteAgent(TEST_NAME_1, TEST_HOST_1, TEST_PORT);

        Host host = hostManager.agentAdded(config);

        assertTrue(host.isRemote());
        assertEquals(TEST_HOST_1, host.getHostName());
        assertEquals(TEST_PORT, host.getPort());
        assertEquals(getExpectedLocation(TEST_HOST_1, TEST_PORT), host.getLocation());
        assertEquals(HostState.PersistentUpgradeState.NONE, host.getPersistentUpgradeState());
    }

    public void testAgentChangedSameLocation()
    {
        AgentConfiguration config = createRemoteAgent(TEST_NAME_1, TEST_HOST_1, TEST_PORT);
        Host host = hostManager.agentAdded(config);

        config.setName(TEST_NAME_2);
        Host newHost = hostManager.agentChanged(config);

        assertSame(host, newHost);
    }

    public void testAgentChangedNewLocation()
    {
        AgentConfiguration config = createRemoteAgent(TEST_NAME_1, TEST_HOST_1, TEST_PORT);
        Host host = hostManager.agentAdded(config);

        config.setHost(TEST_HOST_2);
        Host newHost = hostManager.agentChanged(config);

        assertNotSame(host, newHost);
        assertEquals(getExpectedLocation(TEST_HOST_2, TEST_PORT), newHost.getLocation());
        assertNull(hostManager.getHostForLocation(getExpectedLocation(TEST_HOST_1, TEST_PORT)));
    }

    public void testAgentDeleted()
    {
        AgentConfiguration config = createRemoteAgent(TEST_NAME_1, TEST_HOST_1, TEST_PORT);
        hostManager.agentAdded(config);

        hostManager.agentDeleted(config);

        assertNull(hostManager.getHostForLocation(getExpectedLocation(TEST_HOST_1, TEST_PORT)));
    }

    public void testTwoAgentsDifferentLocations()
    {
        AgentConfiguration config1 = createRemoteAgent(TEST_NAME_1, TEST_HOST_1, TEST_PORT);
        AgentConfiguration config2 = createRemoteAgent(TEST_NAME_2, TEST_HOST_2, TEST_PORT);

        Host host1 = hostManager.agentAdded(config1);
        Host host2 = hostManager.agentAdded(config2);

        assertNotSame(host1, host2);
    }

    public void testTwoAgentsSameLocation()
    {
        AgentConfiguration config1 = createRemoteAgent(TEST_NAME_1, TEST_HOST_1, TEST_PORT);
        AgentConfiguration config2 = createRemoteAgent(TEST_NAME_2, TEST_HOST_1, TEST_PORT);

        Host host1 = hostManager.agentAdded(config1);
        Host host2 = hostManager.agentAdded(config2);

        assertSame(host1, host2);
    }

    public void testAgentDeletedAnotherAgentSameLocation()
    {
        AgentConfiguration config1 = createRemoteAgent(TEST_NAME_1, TEST_HOST_1, TEST_PORT);
        AgentConfiguration config2 = createRemoteAgent(TEST_NAME_2, TEST_HOST_1, TEST_PORT);
        Host host = hostManager.agentAdded(config1);
        hostManager.agentAdded(config2);

        hostManager.agentDeleted(config2);

        assertSame(host, hostManager.getHostForLocation(getExpectedLocation(TEST_HOST_1, TEST_PORT)));
    }

    public void testAgentChangedNoLongerSameLocation()
    {
        AgentConfiguration config1 = createRemoteAgent(TEST_NAME_1, TEST_HOST_1, TEST_PORT);
        AgentConfiguration config2 = createRemoteAgent(TEST_NAME_2, TEST_HOST_1, TEST_PORT);
        Host host1 = hostManager.agentAdded(config1);
        Host host2 = hostManager.agentAdded(config2);

        config2.setHost(TEST_HOST_2);
        Host hostNew = hostManager.agentChanged(config2);

        assertSame(host1, host2);
        assertNotSame(host1, hostNew);
    }

    public void testAgentChangedToSameLocation()
    {
        AgentConfiguration config1 = createRemoteAgent(TEST_NAME_1, TEST_HOST_1, TEST_PORT);
        AgentConfiguration config2 = createRemoteAgent(TEST_NAME_2, TEST_HOST_2, TEST_PORT);
        Host host1 = hostManager.agentAdded(config1);
        Host host2 = hostManager.agentAdded(config2);

        config2.setHost(TEST_HOST_1);
        Host hostNew = hostManager.agentChanged(config2);

        assertNotSame(host1, host2);
        assertSame(host1, hostNew);
    }

    public void testRestoresHostsOnInit()
    {
        HostState state = new HostState();
        state.setRemote(true);
        state.setHostName(TEST_HOST_1);
        state.setPort(TEST_PORT);
        hostStateDao.save(state);

        hostManager.init(agentManager);

        Host host = hostManager.getHostForLocation(getExpectedLocation(TEST_HOST_1, TEST_PORT));
        assertNotNull(host);
        
        Host agentHost = hostManager.agentAdded(createRemoteAgent(TEST_NAME_1, TEST_HOST_1, TEST_PORT));
        assertSame(host, agentHost);
    }

    public void testGetHostForAgent()
    {
        AgentConfiguration config = createRemoteAgent(TEST_NAME_1, TEST_HOST_1, TEST_PORT);
        Host host = hostManager.agentAdded(config);
        
        assertSame(host, hostManager.getHostForAgent(config));
    }

    public void testGetHostForAgentAfterChange()
    {
        AgentConfiguration config = createRemoteAgent(TEST_NAME_1, TEST_HOST_1, TEST_PORT);
        hostManager.agentAdded(config);
        config.setHost(TEST_HOST_2);
        Host host = hostManager.agentChanged(config);
        
        assertSame(host, hostManager.getHostForAgent(config));
    }

    public void testGetAgentsForHost()
    {
        final AgentConfiguration config1 = createRemoteAgent(TEST_NAME_1, TEST_HOST_1, TEST_PORT);
        final AgentConfiguration config2 = createRemoteAgent(TEST_NAME_2, TEST_HOST_1, TEST_PORT);
        Host host = hostManager.agentAdded(config1);
        hostManager.agentAdded(config2);

        Collection<Agent> agents = hostManager.getAgentsForHost(host);
        assertEquals(2, agents.size());
        assertTrue(any(agents, new HasConfig(config1)));
        assertTrue(any(agents, new HasConfig(config2)));
    }

    private String getExpectedLocation(String hostname, int port)
    {
        return hostname + ":" + port;
    }

    private AgentConfiguration createRemoteAgent(String name, String hostname, int port)
    {
        AgentConfiguration config = new AgentConfiguration();
        config.setName(name);
        config.setRemote(true);
        config.setHost(hostname);
        config.setPort(port);
        config.setHandle(nextHandle++);

        Agent agent = new DefaultAgent(config, new AgentState(), mock(AgentService.class), new DefaultHost(new HostState()));
        agentsByHandle.put(config.getHandle(), agent);

        return config;
    }

    private static class HasConfig implements Predicate<Agent>
    {
        private final AgentConfiguration config;

        public HasConfig(AgentConfiguration config)
        {
            this.config = config;
        }

        public boolean apply(Agent agent)
        {
            return agent.getConfig() == config;
        }
    }
}
