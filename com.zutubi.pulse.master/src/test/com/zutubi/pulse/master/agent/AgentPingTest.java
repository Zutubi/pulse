package com.zutubi.pulse.master.agent;

import com.mockobjects.dynamic.C;
import com.mockobjects.dynamic.Mock;
import com.zutubi.pulse.core.test.api.PulseTestCase;
import com.zutubi.pulse.master.tove.config.agent.AgentConfiguration;
import com.zutubi.pulse.servercore.agent.PingStatus;
import com.zutubi.pulse.servercore.services.SlaveStatus;

/**
 */
public class AgentPingTest extends PulseTestCase
{
    private String masterLocation = "test location";
    private int buildNumber = 10;
    private Mock mockAgent;
    private Mock mockService;

    public void setUp() throws Exception
    {
        super.setUp();
        mockAgent = new Mock(Agent.class);
        mockService = new Mock(AgentService.class);
    }

    public void testSuccessfulPing() throws Exception
    {
        SlaveStatus status = new SlaveStatus(PingStatus.IDLE, 12, true);
        mockService.expectAndReturn("ping", buildNumber);
        mockService.expectAndReturn("getStatus", C.args(C.eq(masterLocation)), status);

        AgentPing ping = new AgentPing((Agent) mockAgent.proxy(), (AgentService) mockService.proxy(), buildNumber, masterLocation);
        assertSame(status, ping.call());
        verify();
    }

    public void testFailedPing() throws Exception
    {
        mockService.expectAndThrow("ping", new RuntimeException("bang"));
        mockAgent.expectAndReturn("getConfig", getConfig());

        AgentPing ping = new AgentPing((Agent) mockAgent.proxy(), (AgentService) mockService.proxy(), buildNumber, masterLocation);
        assertEquals(new SlaveStatus(PingStatus.OFFLINE, "Exception: 'java.lang.RuntimeException'. Reason: bang"), ping.call());
        verify();
    }

    public void testFailedGetStatus() throws Exception
    {
        mockService.expectAndReturn("ping", buildNumber);
        mockService.expectAndThrow("getStatus", C.args(C.eq(masterLocation)), new RuntimeException("oops"));
        mockAgent.expectAndReturn("getConfig", getConfig());
        
        AgentPing ping = new AgentPing((Agent) mockAgent.proxy(), (AgentService) mockService.proxy(), buildNumber, masterLocation);
        assertEquals(new SlaveStatus(PingStatus.OFFLINE, "Exception: 'java.lang.RuntimeException'. Reason: oops"), ping.call());
        verify();
    }

    public void testVersionMismatch() throws Exception
    {
        mockService.expectAndReturn("ping", buildNumber - 1);

        AgentPing ping = new AgentPing((Agent) mockAgent.proxy(), (AgentService) mockService.proxy(), buildNumber, masterLocation);
        assertEquals(new SlaveStatus(PingStatus.VERSION_MISMATCH), ping.call());
        verify();
    }

    private AgentConfiguration getConfig()
    {
        AgentConfiguration config = new AgentConfiguration();
        config.setName("slaaave");
        return config;
    }

    private void verify()
    {
        mockAgent.verify();
        mockService.verify();
    }
}
