package com.zutubi.pulse.master.agent;

import com.zutubi.events.DefaultEventManager;
import com.zutubi.events.Event;
import com.zutubi.events.EventListener;
import com.zutubi.events.EventManager;
import com.zutubi.pulse.Version;
import com.zutubi.pulse.core.test.api.PulseTestCase;
import com.zutubi.pulse.master.events.HostPingEvent;
import com.zutubi.pulse.master.scheduling.NoopTrigger;
import com.zutubi.pulse.master.scheduling.Scheduler;
import com.zutubi.pulse.master.security.PulseThreadFactory;
import com.zutubi.pulse.master.tove.config.admin.AgentPingConfiguration;
import com.zutubi.pulse.servercore.agent.PingStatus;
import com.zutubi.pulse.servercore.services.HostStatus;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.stub;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class HostPingServiceTest extends PulseTestCase
{
    private static final String TEST_MASTER = "master.location";
    private static final String TEST_MASTER_URL = "http://" + TEST_MASTER;
    private static final int BUILD_NUMBER = Version.getVersion().getBuildNumberAsInt();

    private BlockingQueue<HostPingEvent> events = new LinkedBlockingQueue<HostPingEvent>();
    private HostPingService hostPingService;

    protected void setUp() throws Exception
    {
        super.setUp();

        EventManager eventManager = new DefaultEventManager();
        eventManager.register(new EventListener()
        {
            public void handleEvent(Event evt)
            {
                events.add((HostPingEvent) evt);
            }

            public Class[] getHandledEvents()
            {
                return new Class[]{HostPingEvent.class};
            }
        });

        hostPingService = new HostPingService();
        hostPingService.setEventManager(eventManager);
        hostPingService.setThreadFactory(new PulseThreadFactory());
        hostPingService.setMasterLocationProvider(new MasterLocationProvider()
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

        Scheduler scheduler = mock(Scheduler.class);
        stub(scheduler.getTrigger(anyString(), anyString())).toReturn(new NoopTrigger());
        hostPingService.setScheduler(scheduler);

        hostPingService.init();
        hostPingService.refreshSettings(new AgentPingConfiguration());
    }

    public void testSimplePing()
    {
        Host host = createHost(1);

        hostPingService.requestPing(host, createHostService(null));
        assertEvent(host);
        assertNoMoreEvents();
    }

    public void testMultiplePings()
    {
        Host host1 = createHost(1);
        HostService service1 = createHostService(null);
        Host host2 = createHost(2);
        HostService service2 = createHostService(null);
        Host host3 = createHost(3);
        HostService service3 = createHostService(null);

        hostPingService.requestPing(host1, service1);
        assertEvent(host1);
        hostPingService.requestPing(host2, service2);
        assertEvent(host2);
        hostPingService.requestPing(host3, service3);
        assertEvent(host3);

        assertNoMoreEvents();
    }

    public void testMultiplePingsSameAgent()
    {
        Host host = createHost(1);
        HostService service = createHostService(null);

        hostPingService.requestPing(host, service);
        assertEvent(host);
        hostPingService.requestPing(host, service);
        assertEvent(host);
        hostPingService.requestPing(host, service);
        assertEvent(host);

        assertNoMoreEvents();
    }

    public void testDuplicateIgnored()
    {
        final Semaphore waitFlag = new Semaphore(0);
        Host host = createHost(1);
        HostService service = createHostService(waitFlag);

        hostPingService.requestPing(host, service);
        hostPingService.requestPing(host, service);

        waitFlag.release();
        waitFlag.release();

        assertEvent(host);
        assertNoMoreEvents();
    }

    public void testTimeout() throws InterruptedException
    {
        AgentPingConfiguration config = new AgentPingConfiguration();
        config.setPingTimeout(1);
        hostPingService.refreshSettings(config);
        
        Semaphore waitFlag = new Semaphore(0);
        Host host = createHost(1);
        HostService service = createHostService(waitFlag);
        
        hostPingService.requestPing(host, service);
        while (hostPingService.isPingInProgress(host))
        {
            Thread.yield();
        }
        waitFlag.release();

        assertEvent(host, PingStatus.OFFLINE, "Host ping timed out");
        assertNoMoreEvents();
    }

    private void assertEvent(Host host)
    {
        assertEvent(host, PingStatus.IDLE, null);
    }

    private void assertEvent(Host host, PingStatus status, String message)
    {
        try
        {
            HostPingEvent event = events.poll(5, TimeUnit.SECONDS);
            assertNotNull(event);
            assertEquals(host.getId(), event.getHost().getId());
            assertEquals(status, event.getHostStatus().getStatus());
            if(message != null)
            {
                assertEquals(message,event.getHostStatus().getMessage());
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

    private Host createHost(long id)
    {
        Host mockHost = mock(Host.class);
        stub(mockHost.getId()).toReturn(id);
        return mockHost;
    }

    /**
     * Creates a host service that will optionally block on a semaphore before
     * returning from a ping call.
     *
     * @param waitFlag if null, pings return immediately, otherwise pings wait
     *                 on this semaphore before returning
     * @return a host service that responds to ping and status requests
     */
    private HostService createHostService(final Semaphore waitFlag)
    {
        HostService mockService = mock(HostService.class);
        if (waitFlag == null)
        {
            stub(mockService.ping()).toReturn(BUILD_NUMBER);
        }
        else
        {
            stub(mockService.ping()).toAnswer(new Answer<Object>()
            {
                public Object answer(InvocationOnMock invocationOnMock) throws Throwable
                {
                    waitFlag.acquire();
                    return BUILD_NUMBER;
                }
            });
        }

        stub(mockService.getStatus(TEST_MASTER_URL)).toReturn(new HostStatus(PingStatus.IDLE));
        return mockService;
    }
}
