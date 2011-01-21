package com.zutubi.pulse.acceptance;

import static com.zutubi.pulse.acceptance.AcceptanceTestUtils.ADMIN_CREDENTIALS;
import com.zutubi.pulse.acceptance.support.Pulse;
import com.zutubi.pulse.acceptance.support.PulsePackage;
import com.zutubi.pulse.acceptance.support.jython.JythonPulseTestFactory;
import com.zutubi.pulse.core.test.api.PulseTestCase;
import com.zutubi.pulse.core.ui.api.YesNoResponse;
import com.zutubi.pulse.core.util.config.EnvConfig;
import com.zutubi.pulse.core.util.process.AsyncProcess;
import com.zutubi.pulse.core.util.process.BufferingCharHandler;
import com.zutubi.pulse.dev.bootstrap.DefaultDevPaths;
import com.zutubi.pulse.dev.bootstrap.DevPaths;
import com.zutubi.util.FileSystemUtils;
import com.zutubi.util.StringUtils;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import static java.util.Arrays.asList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Base helper class for tests the work with dev tools packages/commands.
 */
public class DevToolsTestBase extends PulseTestCase
{
    protected static final long COMMAND_TIMEOUT_SECS = 300;
    
    protected static final String COMMAND_EXPAND = "expand";
    protected static final String COMMAND_LOCAL = "local";
    protected static final String COMMAND_PERSONAL = "personal";
    protected static final String COMMAND_PROCESS = "process";
    protected static final String COMMAND_SYNCHRONISE = "synchronise";

    protected static final String FLAG_NO_UPDATE = "--no-update";
    protected static final String FLAG_PASSWORD = "-p";
    protected static final String FLAG_PATCH_FILE = "-f";
    protected static final String FLAG_PATCH_TYPE = "-t";
    protected static final String FLAG_PROJECT = "-r";
    protected static final String FLAG_REVISION = "-e";
    protected static final String FLAG_SERVER = "-s";
    protected static final String FLAG_USER = "-u";
    
    protected static final String PROMPT_NO_PLUGINS = "Synchronise plugins with a Pulse master now?";
    protected static final String OUTPUT_SYNC_COMPLETE = "Synchronisation complete.";
    
    protected File tmpDir;
    protected Pulse pulse;
    protected DevPaths devPaths;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();

        JythonPulseTestFactory factory = new JythonPulseTestFactory();

        tmpDir = FileSystemUtils.createTempDir(getName(), ".tmp");
        File userHomeDir = new File(tmpDir, EnvConfig.USER_HOME);
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
    
    protected String inputLines(String... lines)
    {
        return StringUtils.join("\n", lines) + "\n";
    }
    
    protected String getServerSetupInputLines()
    {
        return inputLines(YesNoResponse.YES.name(), YesNoResponse.YES.name(), AcceptanceTestUtils.getPulseUrl(), ADMIN_CREDENTIALS.getUserName());
    }

    protected String runCommand(String... command) throws Exception
    {
        return runCommandWithInput(null, command);
    }
    
    protected String runCommandWithInput(String input, String... command) throws Exception
    {
        List<String> fullCommand = new LinkedList<String>();
        fullCommand.add(pulse.getScript());
        fullCommand.addAll(asList(command));
        ProcessBuilder builder = new ProcessBuilder(fullCommand);
        builder.directory(tmpDir);
        builder.environment().put("PULSE_HOME", pulse.getPulseHome());

        AsyncProcess process = null;
        try
        {
            BufferingCharHandler handler = new BufferingCharHandler();
            
            Process child = builder.start();
            process = new AsyncProcess(child, handler, true);

            if (input != null)
            {
                try
                {
                    OutputStream stdinStream = child.getOutputStream();
                    stdinStream.write(input.getBytes());
                    stdinStream.close();
                }
                catch (IOException e)
                {
                    throw new Exception("Error writing to input of dev tools process", e);
                }
            }

            int exitCode = process.waitForOrThrow(COMMAND_TIMEOUT_SECS, TimeUnit.SECONDS);
            assertEquals("Non-zero exit code from command: " + exitCode +
                    "\n[stdout]\n" + handler.getStdout() + "\n[/stdout]" +
                    "\n[stderr]\n" + handler.getStderr() + "[/stderr]\n",
                    0, exitCode);

            assertEquals("", handler.getStderr());
            return handler.getStdout();
        }
        finally
        {
            if (process != null)
            {
                process.destroy();
            }
        }
    }

    protected String runPluginSync() throws Exception
    {
        String output = runCommand(COMMAND_SYNCHRONISE, FLAG_SERVER, AcceptanceTestUtils.getPulseUrl());
        
        // Quick check for good and bad signs.
        String lowerOutput = output.toLowerCase();
        assertThat(lowerOutput, containsString("actions determined"));
        assertThat(lowerOutput, not(containsString("error")));
        assertThat(lowerOutput, not(containsString("exception")));
        
        return output;
    }
}
