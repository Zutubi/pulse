package com.zutubi.pulse.master.agent;

import com.zutubi.events.DefaultEventManager;
import com.zutubi.events.Event;
import com.zutubi.events.EventListener;
import com.zutubi.events.EventManager;
import com.zutubi.pulse.Version;
import com.zutubi.pulse.core.test.api.PulseTestCase;
import com.zutubi.pulse.master.bootstrap.SimpleMasterConfigurationManager;
import com.zutubi.pulse.master.events.HostUpgradeCompleteEvent;
import com.zutubi.pulse.master.model.HostState;
import com.zutubi.pulse.master.servlet.DownloadPackageServlet;
import com.zutubi.pulse.servercore.bootstrap.DefaultSystemPaths;
import com.zutubi.pulse.servercore.bootstrap.SystemPaths;
import com.zutubi.pulse.servercore.services.UpgradeState;
import com.zutubi.pulse.servercore.services.UpgradeStatus;
import com.zutubi.util.FileSystemUtils;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.io.File;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

public class HostUpdaterTest extends PulseTestCase implements EventListener
{
    private static final String TEST_URL = "url";

    private File tempDir;
    private HostState hostState;
    private HostService hostService;
    private DefaultHost host;
    private HostUpdater updater;
    private Semaphore hostSemaphore;
    private HostUpgradeCompleteEvent event;
    private Semaphore eventSemaphore;

    protected void setUp() throws Exception
    {
        super.setUp();

        // Create dummy paths and package file
        tempDir = FileSystemUtils.createTempDir(HostUpdaterTest.class.getName(), "");
        SystemPaths systemPaths = new DefaultSystemPaths(tempDir, tempDir);
        File packageDir = DownloadPackageServlet.getPackageDir(systemPaths);
        assertTrue(packageDir.mkdirs());
        File packageFile = DownloadPackageServlet.getAgentZip(systemPaths);
        FileSystemUtils.createFile(packageFile, "dummy");

        hostState = new HostState();
        hostState.setId(222);
        EventManager eventManager = new DefaultEventManager();
        eventManager.register(this);

        hostService = mock(HostService.class);
        host = new TestHost(hostState);

        updater = new HostUpdater(host, hostService);
        updater.setStatusTimeout(5);
        updater.setRebootTimeout(5);
        updater.setPingInterval(10);

        SimpleMasterConfigurationManager configurationManager = new SimpleMasterConfigurationManager();
        configurationManager.setSystemPaths(systemPaths);
        updater.setConfigurationManager(configurationManager);
        updater.setEventManager(eventManager);
        MasterLocationProvider masterLocationProvider = mock(MasterLocationProvider.class);
        stub(masterLocationProvider.getMasterUrl()).toReturn(TEST_URL);
        updater.setMasterLocationProvider(masterLocationProvider);
        updater.setThreadFactory(Executors.defaultThreadFactory());

        hostSemaphore = new Semaphore(0);
        eventSemaphore = new Semaphore(0);
    }

    protected void tearDown() throws Exception
    {
        removeDirectory(tempDir);
        super.tearDown();
    }

    public void testExpectedSequence() throws Exception
    {
        configureService(true, 3);
        updater.start();
        start();
        download();
        apply();
        reboot();
        syncPlugins();
        reboot();
        assertCompleted(true);
    }

    public void testNoPluginUpdates() throws Exception
    {
        configureService(true, 3);
        doReturn(false).when(hostService).syncPlugins(anyString(), anyLong(), anyString());

        updater.start();
        start();
        download();
        apply();
        reboot();
        assertCompleted(true);
    }
    
    public void testOnlyPluginUpdates() throws Exception
    {
        doReturn(Version.getVersion().getBuildNumberAsInt()).when(hostService).ping();
        doReturn(true).when(hostService).syncPlugins(anyString(), anyLong(), anyString());

        updater.start();
        syncPlugins();
        reboot();
        assertCompleted(true);
    }
    
    public void testHostRejects() throws Exception
    {
        configureService(false, 0);
        updater.start();
        assertCompleted(false);
        assertEquals(host.getUpgradeState(), UpgradeState.FAILED);
        assertTrue(host.getUpgradeMessage().contains("rejected"));
    }

    public void testImmediateError() throws Exception
    {
        configureService(true, 0);
        updater.start();
        updater.upgradeStatus(new UpgradeStatus(hostState.getId(), UpgradeState.ERROR, -1, "Exploded"));
        assertStatus(UpgradeState.ERROR, -1, "Exploded");
        assertCompleted(false);
    }

    public void testDownloadError() throws Exception
    {
        configureService(true, 0);
        updater.start();
        start();
        download();
        updater.upgradeStatus(new UpgradeStatus(hostState.getId(), UpgradeState.ERROR, -1, "Download failed"));
        assertStatus(UpgradeState.ERROR, -1, "Download failed");
        assertCompleted(false);
    }

    public void testImmediateStatusTimeout() throws Exception
    {
        configureService(true, 0);
        updater.setStatusTimeout(1);
        updater.start();
        assertCompleted(false);
        assertStatus(UpgradeState.FAILED, -1, "Timed out waiting for message from host.");
    }

    public void testTimeout() throws Exception
    {
        configureService(true, 0);
        updater.setStatusTimeout(1);
        updater.start();
        start();
        download();
        assertCompleted(false);
        assertStatus(UpgradeState.FAILED, -1, "Timed out waiting for message from host.");
    }

    public void testRebootTimeout() throws Exception
    {
        configureService(true, 10);
        updater.setRebootTimeout(1);
        updater.setPingInterval(250);
        updater.start();
        start();
        download();
        apply();
        reboot();
        assertCompleted(false);
        assertStatus(UpgradeState.FAILED, -1, "Timed out waiting for host to reboot.");
    }

    public void testRebootWrongBuild() throws Exception
    {
        configureService(true, 2, 6);
        updater.setPingInterval(250);
        updater.setRebootTimeout(1);
        updater.start();
        start();
        download();
        apply();
        reboot();
        assertCompleted(false);
        int expectedVersion = Version.getVersion().getBuildNumberAsInt();
        assertStatus(UpgradeState.FAILED, -1, "Host failed to upgrade to expected build.  Expected build "+expectedVersion+" but found 6");
    }

    public void testErrorDuringPluginUpdates() throws Exception
    {
        doReturn(Version.getVersion().getBuildNumberAsInt()).when(hostService).ping();
        doReturn(true).when(hostService).syncPlugins(anyString(), anyLong(), anyString());

        updater.start();
        syncPlugins();
        updater.upgradeStatus(new UpgradeStatus(host.getId(), UpgradeState.ERROR, -1, "badness"));
        assertCompleted(false);
    }
    
    private void configureService(boolean accept, int successfulPing)
    {
        configureService(accept, successfulPing, Version.getVersion().getBuildNumberAsInt());
    }

    private void configureService(boolean accept, int successfulPing, final int build)
    {
        doReturn(accept).when(hostService).updateVersion(anyString(), anyString(), anyLong(), anyString(), anyLong());
        configurePing(successfulPing, true, build);
        doReturn(true).when(hostService).syncPlugins(anyString(), anyLong(), anyString());
    }

    private void configurePing(int successfulPing, final boolean firstTime, final int build)
    {
        final boolean[] first = new boolean[]{firstTime};
        final int[] remaining = new int[]{successfulPing};
        doAnswer(new Answer<Object>()
        {
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable
            {
                if (first[0])
                {
                    first[0] = false;
                    return build - 1;
                }
                else
                {
                    if (remaining[0] > 0)
                    {
                        remaining[0] = remaining[0] - 1;
                        throw new RuntimeException("Failed ping");
                    }
                    else
                    {
                        return build;
                    }
                }
            }
        }).when(hostService).ping();
    }

    private void start() throws Exception
    {
        updater.upgradeStatus(new UpgradeStatus(hostState.getId(), UpgradeState.STARTED, -1, null));
        assertStatus(UpgradeState.STARTED, -1, null);
    }

    private void download() throws Exception
    {
        updater.upgradeStatus(new UpgradeStatus(hostState.getId(), UpgradeState.DOWNLOADING, -1, null));
        assertStatus(UpgradeState.DOWNLOADING, -1, null);
    }

    private void apply() throws Exception
    {
        updater.upgradeStatus(new UpgradeStatus(hostState.getId(), UpgradeState.APPLYING, -1, null));
        assertStatus(UpgradeState.APPLYING, -1, null);
    }

    private void reboot() throws Exception
    {
        updater.upgradeStatus(new UpgradeStatus(hostState.getId(), UpgradeState.REBOOTING, -1, null));
        assertStatus(UpgradeState.REBOOTING, -1, null);
    }

    private void syncPlugins() throws Exception
    {
        assertStatus(UpgradeState.SYNCHRONISING_PLUGINS, -1, null);
        configurePing(3, false, Version.getVersion().getBuildNumberAsInt());
    }
    
    private void assertStatus(UpgradeState state, int progress, String message) throws Exception
    {
        assertTrue(hostSemaphore.tryAcquire(10, TimeUnit.SECONDS));
        assertEquals(state, host.getUpgradeState());
        assertEquals(progress, host.getUpgradeProgress());
        assertEquals(message, host.getUpgradeMessage());
    }

    private void assertCompleted(boolean succeeded) throws Exception
    {
        assertTrue(eventSemaphore.tryAcquire(10, TimeUnit.SECONDS));
        assertEquals(host, event.getHost());
        assertEquals(succeeded, event.isSuccessful());
    }

    public void handleEvent(Event evt)
    {
        event = (HostUpgradeCompleteEvent) evt;
        eventSemaphore.release();
    }

    public Class[] getHandledEvents()
    {
        return new Class[] { HostUpgradeCompleteEvent.class };
    }

    private class TestHost extends DefaultHost
    {
        private TestHost(HostState state)
        {
            super(state);
        }

        void upgradeStatus(UpgradeState state, int progress, String message)
        {
            super.upgradeStatus(state, progress, message);
            hostSemaphore.release();
        }
    }
}
