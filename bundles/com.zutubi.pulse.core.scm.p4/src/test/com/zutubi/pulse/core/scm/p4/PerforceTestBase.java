package com.zutubi.pulse.core.scm.p4;

import com.zutubi.pulse.core.PulseExecutionContext;
import com.zutubi.pulse.core.engine.api.BuildProperties;
import com.zutubi.pulse.core.engine.api.ExecutionContext;
import com.zutubi.pulse.core.test.TestUtils;
import com.zutubi.pulse.core.test.api.PulseTestCase;
import com.zutubi.util.FileSystemUtils;

import java.io.File;
import java.io.IOException;

public abstract class PerforceTestBase extends PulseTestCase
{
    private static final String COMMAND_P4D = "p4d";

    private static final String FLAG_UNICODE_SWITCH = "-xi";
    private static final String FLAG_JOURNAL_RESTORE = "-jr";
    private static final String FLAG_PORT = "-p";
    private static final String FLAG_ROOT_DIRECTORY = "-r";

    protected static final int P4D_PORT = 6666;

    protected static final String TEST_PROJECT = "test project";
    protected static final long TEST_PROJECT_HANDLE = 11;
    protected static final String TEST_AGENT = "test agent";
    protected static final long TEST_AGENT_HANDLE = 22;

    private Process p4dProcess;
    protected File tmpDir;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();

        tmpDir = FileSystemUtils.createTempDir(PerforceTestBase.class.getName(), "");
        tmpDir = tmpDir.getCanonicalFile();
    }

    @Override
    protected void tearDown() throws Exception
    {
        p4dProcess.destroy();
        Thread.sleep(400);
        removeDirectory(tmpDir);

        super.tearDown();
    }

    public String getP4Port()
    {
        return ":" + P4D_PORT;
    }

    protected ExecutionContext createExecutionContext(File dir, boolean incrementalBootstrap)
    {
        PulseExecutionContext context = new PulseExecutionContext();
        context.setWorkingDir(dir);
        context.addValue(BuildProperties.NAMESPACE_INTERNAL, BuildProperties.PROPERTY_INCREMENTAL_BOOTSTRAP, incrementalBootstrap);
        context.addString(BuildProperties.NAMESPACE_INTERNAL, BuildProperties.PROPERTY_PROJECT, TEST_PROJECT);
        context.addValue(BuildProperties.NAMESPACE_INTERNAL, BuildProperties.PROPERTY_PROJECT_HANDLE, TEST_PROJECT_HANDLE);
        context.addString(BuildProperties.NAMESPACE_INTERNAL, BuildProperties.PROPERTY_AGENT, TEST_AGENT);
        context.addValue(BuildProperties.NAMESPACE_INTERNAL, BuildProperties.PROPERTY_AGENT_HANDLE, TEST_AGENT_HANDLE);
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
    protected void deployPerforceServer(String name, int port, int checkpoint, boolean unicode) throws Exception
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

    protected void restoreCheckpoint(String repositoryPath, String checkpoint) throws InterruptedException, IOException
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

    protected void startServer(String repositoryPath, int port) throws Exception
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

    protected void switchToUnicode(String repositoryPath) throws Exception
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
