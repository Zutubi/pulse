package com.zutubi.pulse.agent;

import com.mockobjects.dynamic.C;
import com.mockobjects.dynamic.Mock;
import com.zutubi.pulse.BuildService;
import com.zutubi.pulse.Version;
import com.zutubi.pulse.bootstrap.DefaultSystemPaths;
import com.zutubi.pulse.bootstrap.SystemPaths;
import com.zutubi.pulse.events.*;
import com.zutubi.pulse.model.Slave;
import com.zutubi.pulse.services.ServiceTokenManager;
import com.zutubi.pulse.services.SlaveService;
import com.zutubi.pulse.services.UpgradeState;
import com.zutubi.pulse.services.UpgradeStatus;
import com.zutubi.pulse.servlet.DownloadPackageServlet;
import com.zutubi.pulse.test.PulseTestCase;
import com.zutubi.pulse.util.FileSystemUtils;

import java.io.File;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 */
public class AgentUpdaterTest extends PulseTestCase implements EventListener
{
    private static final String TEST_TOKEN = "token";
    private static final String TEST_URL = "url";

    private File tempDir;
    private SystemPaths systemPaths;
    private Slave slave;
    private SlaveAgent agent;
    private EventManager eventManager;
    private Mock mockService;
    private AgentUpdater updater;
    private Semaphore agentSemaphore;
    private SlaveUpgradeCompleteEvent event;
    private Semaphore eventSemaphore;

    protected void setUp() throws Exception
    {
        super.setUp();

        // Create dummy paths and package file
        tempDir = FileSystemUtils.createTempDir(AgentUpdaterTest.class.getName(), "");
        systemPaths = new DefaultSystemPaths(tempDir, tempDir);
        File packageDir = DownloadPackageServlet.getPackageDir(systemPaths);
        packageDir.mkdirs();
        File packageFile = DownloadPackageServlet.getAgentZip(systemPaths);
        FileSystemUtils.createFile(packageFile, "dummy");

        slave = new Slave("test", "host");
        slave.setId(111);
        eventManager = new DefaultEventManager();
        eventManager.register(this);

        mockService = new Mock(SlaveService.class);
        agentSemaphore = new Semaphore(0);
        eventSemaphore = new Semaphore(0);
    }

    protected void tearDown() throws Exception
    {
        FileSystemUtils.rmdir(tempDir);
        super.tearDown();
    }

    public void testExpectedSequence() throws Exception
    {
        createUpdater(true, 3);
        updater.start();
        start();
        download();
        apply();
        reboot();
        assertCompleted(true);
    }

    public void testAgentRejects() throws Exception
    {
        createUpdater(false, 0);
        updater.start();
        assertCompleted(false);
        assertEquals(agent.getUpgradeState(), UpgradeState.FAILED);
        assertTrue(agent.getUpgradeMessage().contains("rejected"));
    }

    public void testImmediateError() throws Exception
    {
        createUpdater(true, 0);
        updater.start();
        updater.upgradeStatus(new UpgradeStatus(slave.getId(), UpgradeState.ERROR, -1, "Exploded"));
        assertStatus(UpgradeState.ERROR, -1, "Exploded");
        assertCompleted(false);
    }

    public void testDownloadError() throws Exception
    {
        createUpdater(true, 0);
        updater.start();
        start();
        download();
        updater.upgradeStatus(new UpgradeStatus(slave.getId(), UpgradeState.ERROR, -1, "Download failed"));
        assertStatus(UpgradeState.ERROR, -1, "Download failed");
        assertCompleted(false);
    }

    public void testImmediateStatusTimeout() throws Exception
    {
        createUpdater(true, 0);
        updater.setStatusTimeout(1);
        updater.start();
        assertCompleted(false);
        assertStatus(UpgradeState.FAILED, -1, "Timed out waiting for message from agent.");
    }

    public void testTimeout() throws Exception
    {
        createUpdater(true, 0);
        updater.setStatusTimeout(1);
        updater.start();
        start();
        download();
        assertCompleted(false);
        assertStatus(UpgradeState.FAILED, -1, "Timed out waiting for message from agent.");
    }

    public void testRebootTimeout() throws Exception
    {
        createUpdater(true, 10);
        updater.setPingInterval(1000);
        updater.start();
        start();
        download();
        apply();
        reboot();
        assertCompleted(false);
        assertStatus(UpgradeState.FAILED, -1, "Timed out waiting for agent to reboot.");
    }

    public void testRebootWrongBuild() throws Exception
    {
        createUpdater(true, 2, 6);
        updater.setPingInterval(5000);
        updater.start();
        start();
        download();
        apply();
        reboot();
        assertCompleted(false);
        assertStatus(UpgradeState.FAILED, -1, "Timed out waiting for agent to reboot.");
    }

    private void createUpdater(boolean accept, int successfulPing)
    {
        createUpdater(accept, successfulPing, Version.getVersion().getBuildNumberAsInt());
    }

    private void createUpdater(boolean accept, int successfulPing, int build)
    {
        mockService.expectAndReturn("updateVersion", C.ANY_ARGS, accept);
        if(successfulPing > 0)
        {
            for(int i = 0; i < successfulPing - 1; i++)
            {
                mockService.expectAndThrow("ping", C.NO_ARGS, new RuntimeException("Failed ping"));
            }
        }

        if(successfulPing >= 0)
        {
            for(int i = 0; i < 100; i++)
            {
                mockService.expectAndReturn("ping", C.NO_ARGS, build);
            }
        }

        agent = new MockSlaveAgent(slave, (SlaveService) mockService.proxy(), null, null);
        updater = new AgentUpdater(agent, TEST_TOKEN, TEST_URL, eventManager, systemPaths);
        updater.setStatusTimeout(5);
        updater.setRebootTimeout(5);
        updater.setPingInterval(10);
    }

    private void start() throws Exception
    {
        updater.upgradeStatus(new UpgradeStatus(slave.getId(), UpgradeState.STARTED, -1, null));
        assertStatus(UpgradeState.STARTED, -1, null);
    }

    private void download() throws Exception
    {
        updater.upgradeStatus(new UpgradeStatus(slave.getId(), UpgradeState.DOWNLOADING, -1, null));
        assertStatus(UpgradeState.DOWNLOADING, -1, null);
    }

    private void apply() throws Exception
    {
        updater.upgradeStatus(new UpgradeStatus(slave.getId(), UpgradeState.APPLYING, -1, null));
        assertStatus(UpgradeState.APPLYING, -1, null);
    }

    private void reboot() throws Exception
    {
        updater.upgradeStatus(new UpgradeStatus(slave.getId(), UpgradeState.REBOOTING, -1, null));
        assertStatus(UpgradeState.REBOOTING, -1, null);
    }

    private void assertStatus(UpgradeState state, int progress, String message) throws Exception
    {
        assertTrue(agentSemaphore.tryAcquire(10, TimeUnit.SECONDS));
        assertEquals(state, agent.getUpgradeState());
        assertEquals(progress, agent.getUpgradeProgress());
        assertEquals(message, agent.getUpgradeMessage());
    }

    private void assertCompleted(boolean succeeded) throws Exception
    {
        assertTrue(eventSemaphore.tryAcquire(10, TimeUnit.SECONDS));
        assertEquals(agent, event.getAgent());
        assertEquals(succeeded, event.isSuccessful());
    }

    public void handleEvent(Event evt)
    {
        event = (SlaveUpgradeCompleteEvent) evt;
        eventSemaphore.release();
    }

    public Class[] getHandledEvents()
    {
        return new Class[] { SlaveUpgradeCompleteEvent.class };
    }

    private class MockSlaveAgent extends SlaveAgent
    {
        public MockSlaveAgent(Slave slave, SlaveService slaveService, ServiceTokenManager serviceTokenManager, BuildService buildService)
        {
            super(slave, slaveService, serviceTokenManager, buildService);
        }

        public void upgradeStatus(UpgradeState state, int progress, String message)
        {
            super.upgradeStatus(state, progress, message);
            agentSemaphore.release();
        }
    }
}
