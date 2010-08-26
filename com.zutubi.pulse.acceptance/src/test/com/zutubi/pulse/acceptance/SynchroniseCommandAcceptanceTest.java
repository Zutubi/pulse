package com.zutubi.pulse.acceptance;

import com.zutubi.pulse.acceptance.support.Pulse;
import com.zutubi.pulse.acceptance.support.PulsePackage;
import com.zutubi.pulse.acceptance.support.jython.JythonPulseTestFactory;
import com.zutubi.pulse.core.plugins.repository.PluginInfo;
import com.zutubi.pulse.core.plugins.repository.PluginRepository;
import com.zutubi.pulse.core.plugins.repository.http.HttpPluginRepository;
import com.zutubi.pulse.core.test.api.PulseTestCase;
import com.zutubi.pulse.core.util.process.AsyncProcess;
import com.zutubi.pulse.core.util.process.BufferingCharHandler;
import com.zutubi.pulse.dev.bootstrap.DefaultDevPaths;
import com.zutubi.pulse.dev.bootstrap.DevPaths;
import com.zutubi.pulse.master.servlet.PluginRepositoryServlet;
import com.zutubi.util.FileSystemUtils;
import org.apache.commons.io.filefilter.SuffixFileFilter;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;

public class SynchroniseCommandAcceptanceTest extends PulseTestCase
{
    private static final String MASTER_URL = "http://localhost:" + AcceptanceTestUtils.getPulsePort();
    private static final long SYNC_TIMEOUT_SECS = 300;
    
    private File tmpDir;
    private Pulse pulse;
    private DevPaths devPaths;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();

        JythonPulseTestFactory factory = new JythonPulseTestFactory();

        tmpDir = FileSystemUtils.createTempDir(getName(), ".tmp");
        File userHomeDir = new File(tmpDir, "user.home");
        assertTrue(userHomeDir.mkdir());
        
        File devPackage = AcceptanceTestUtils.getDevPackage();
        PulsePackage pkg = factory.createPackage(devPackage);
        pulse = pkg.extractTo(new File(tmpDir, "pulse.home").getCanonicalPath());
        pulse.setUserHome(userHomeDir.getCanonicalPath());

        String packageName = devPackage.getName();
        String[] pieces = packageName.split("-");
        pieces = pieces[2].split("\\.");
        String userRoot = ".pulse" + pieces[0] + pieces[1] + "-dev";
        devPaths = new DefaultDevPaths(new File(userHomeDir, userRoot), new File(pulse.getActiveVersionDirectory()));
    }

    @Override
    protected void tearDown() throws Exception
    {
        FileSystemUtils.rmdir(tmpDir);
        super.tearDown();
    }
    
    public void testSyncNewInstall() throws Exception
    {
        File pluginDir = devPaths.getPluginStorageDir();

        assertFalse(pluginDir.exists());
        runSync();
        assertTrue(pluginDir.exists());

        HttpPluginRepository repository = new HttpPluginRepository(MASTER_URL + "/" + PluginRepositoryServlet.PATH_REPOSITORY);
        List<PluginInfo> corePlugins = repository.getAvailablePlugins(PluginRepository.Scope.CORE);
        assertEquals(corePlugins.size(), pluginDir.list(new SuffixFileFilter(".jar")).length);
    }

    public void testSecondSyncDoesNothing() throws Exception
    {
        runSync();

        File pluginDir = devPaths.getPluginStorageDir();
        Set<String> pluginListing = new HashSet<String>(asList(pluginDir.list()));

        String output = runSync();
        assertThat(output, containsString("Plugins are already up-to-date."));
        assertEquals(pluginListing, new HashSet<String>(asList(pluginDir.list())));
    }
    
    private String runSync() throws Exception
    {
        ProcessBuilder builder = new ProcessBuilder(pulse.getScript(), "synchronise", "-s", MASTER_URL);
        BufferingCharHandler handler = new BufferingCharHandler();
        AsyncProcess process = new AsyncProcess(builder.start(), handler, true);

        int exitCode = process.waitForOrThrow(SYNC_TIMEOUT_SECS, TimeUnit.SECONDS);
        assertEquals("Non-zero exit code from sync command: " + exitCode +
                "\n[stdout]\n" + handler.getStdout() + "\n[/stdout]" +
                "\n[stderr]\n" + handler.getStderr() + "[/stderr]\n",
                0, exitCode);
        
        // Quick check for good and bad signs.
        assertEquals("", handler.getStderr());
        String lowerOutput = handler.getStdout().toLowerCase();
        assertThat(lowerOutput, containsString("actions determined"));
        assertThat(lowerOutput, not(containsString("error")));
        assertThat(lowerOutput, not(containsString("exception")));
        
        return handler.getStdout();
    }
}
