package com.zutubi.pulse.core.scm.p4;

import com.zutubi.pulse.core.PulseExecutionContext;
import com.zutubi.pulse.core.engine.api.ExecutionContext;
import com.zutubi.pulse.core.scm.p4.config.PerforceConfiguration;
import com.zutubi.pulse.core.test.TestUtils;
import com.zutubi.pulse.core.test.api.PulseTestCase;
import com.zutubi.util.FileSystemUtils;

import java.io.File;
import java.io.IOException;

/**
 * Set of tests to verify the ability of the Perforce Client to communicate
 * with a unicode server.
 */
public class PerforceUnicodeTest extends PulseTestCase
{
    private static final String COMMAND_P4D = "p4d";

    private static final String FLAG_JOURNAL_RESTORE = "-jr";
    private static final String FLAG_PORT = "-p";
    private static final String FLAG_ROOT_DIRECTORY = "-r";
    private static final String FLAG_UNICODE_SWITCH = "-xi";

    private static final int P4D_PORT = 6666;

    private File tmpDir;
    private File workDir;

    private Process p4dProcess;
    private PerforceConfiguration configuration;
    private PerforceClient client;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();

        tmpDir = FileSystemUtils.createTempDir(getClass().getName(), "");
        tmpDir = tmpDir.getCanonicalFile();

        workDir = new File(tmpDir, "work");

        configuration = new PerforceConfiguration(Integer.toString(P4D_PORT), "test-user", "", "daniel-PC");
    }

    @Override
    protected void tearDown() throws Exception
    {
        try
        {
            if (p4dProcess != null)
            {
                p4dProcess.destroy();
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        removeDirectory(tmpDir);

        super.tearDown();
    }

    public void testNonUnicodeServerCheckout() throws Exception
    {
        deployPerforceServer("snapshot", P4D_PORT, 1, false);

        client = new PerforceClient(configuration, new PerforceWorkspaceManager());
        client.checkout(createExecutionContext(workDir), null, null);

        assertTrue(new File(workDir, "readme.txt").isFile());
        assertTrue(new File(workDir, "doc/readme.txt").isFile());
    }

    public void testUnicodeServerCheckout() throws Exception
    {
        deployPerforceServer("snapshot", P4D_PORT, 1, true);
        configuration.setUnicodeServer(true);
        configuration.setCharset("utf8");

        client = new PerforceClient(configuration, new PerforceWorkspaceManager());
        client.checkout(createExecutionContext(workDir), null, null);

        assertTrue(new File(workDir, "readme.txt").isFile());
        assertTrue(new File(workDir, "doc/readme.txt").isFile());
    }

    protected ExecutionContext createExecutionContext(File dir)
    {
        PulseExecutionContext context = new PulseExecutionContext();
        context.setWorkingDir(dir);
        return context;
    }

    /**
     * Deploy a perforce server, using the content of the named zip as the repository root.
     * The server will be started on the specified port, at the indicated checkpoint.
     *
     * @param name          the name of the perforce zip
     * @param port          the port on which the perforce server will start.
     * @param checkpoint    the checkpoint number for the starting state of the repository.
     * @param unicode       indicates whether or not the server should be started in unicode mode.
     * @throws Exception    is thrown on error.
     */
    private void deployPerforceServer(String name, int port, int checkpoint, boolean unicode) throws Exception
    {
        File repoDir = new File(tmpDir, name);
        assertTrue(repoDir.mkdir());

        unzipInput(name, repoDir);

        // Restore from checkpoint
        restoreCheckpoint(repoDir.getAbsolutePath(), "checkpoint." + checkpoint);
        if (unicode)
        {
            switchToUnicode(repoDir.getAbsolutePath());
        }
        startServer(repoDir.getAbsolutePath(), port);
    }

    private void restoreCheckpoint(String repositoryPath, String checkpoint) throws InterruptedException, IOException
    {
        Process process = Runtime.getRuntime().exec(new String[] {
                COMMAND_P4D, FLAG_ROOT_DIRECTORY, repositoryPath, FLAG_JOURNAL_RESTORE, checkpoint
        });
        int resultStatus = process.waitFor();
        if (resultStatus != 0)
        {
            throw new RuntimeException("Restore checkpoint exited with status " + resultStatus);
        }
    }

    private void startServer(String repositoryPath, int port) throws Exception
    {
        p4dProcess = Runtime.getRuntime().exec(new String[] {
                COMMAND_P4D, FLAG_ROOT_DIRECTORY, repositoryPath, FLAG_PORT,  Integer.toString(port)
        });
        try
        {
            TestUtils.waitForServer(port);
        }
        catch (Exception e)
        {
            p4dProcess.destroy();
            throw e;
        }
    }

    private void switchToUnicode(String repositoryPath) throws Exception
    {
        Process process = Runtime.getRuntime().exec(new String[] {
                COMMAND_P4D, FLAG_ROOT_DIRECTORY, repositoryPath, FLAG_UNICODE_SWITCH
        });
        int status = process.waitFor();
        if (status != 0)
        {
            throw new RuntimeException("Switch to unicode exited with status " + status);
        }
    }
}
