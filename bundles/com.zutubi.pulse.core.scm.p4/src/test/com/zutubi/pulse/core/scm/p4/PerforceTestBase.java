package com.zutubi.pulse.core.scm.p4;

import com.zutubi.pulse.core.PulseExecutionContext;
import com.zutubi.pulse.core.engine.api.BuildProperties;
import com.zutubi.pulse.core.engine.api.ExecutionContext;
import com.zutubi.pulse.core.test.TestUtils;
import com.zutubi.pulse.core.test.api.PulseTestCase;
import com.zutubi.util.FileSystemUtils;

import java.io.File;

public abstract class PerforceTestBase extends PulseTestCase
{
    private static final String DIR_REPO = "repo";

    private static final String COMMAND_P4D = "p4d";

    private static final String FLAG_JOURNAL_RESTORE = "-jr";
    private static final String FLAG_PORT = "-p";
    private static final String FLAG_ROOT_DIRECTORY = "-r";

    private static final int P4D_PORT = 6666;

    protected static final String TEST_PROJECT = "test project";
    protected static final long TEST_PROJECT_HANDLE = 11;
    protected static final String TEST_AGENT = "test agent";
    protected static final long TEST_AGENT_HANDLE = 22;

    private File tempDir;
    private Process p4dProcess;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();

        tempDir = FileSystemUtils.createTempDir(PerforceWorkingCopyTest.class.getName(), "");
        tempDir = tempDir.getCanonicalFile();
        File repoDir = new File(tempDir, DIR_REPO);
        assertTrue(repoDir.mkdir());

        unzipInput("repo", repoDir);

        // Restore from checkpoint
        p4dProcess = Runtime.getRuntime().exec(new String[] {COMMAND_P4D, FLAG_ROOT_DIRECTORY, repoDir.getAbsolutePath(), FLAG_JOURNAL_RESTORE, getCheckpointFilename()});
        p4dProcess.waitFor();

        p4dProcess = Runtime.getRuntime().exec(new String[] {COMMAND_P4D, FLAG_ROOT_DIRECTORY, repoDir.getAbsolutePath(), FLAG_PORT,  Integer.toString(P4D_PORT)});
        TestUtils.waitForServer(P4D_PORT);
    }

    @Override
    protected void tearDown() throws Exception
    {
        p4dProcess.destroy();
        Thread.sleep(400);
        removeDirectory(tempDir);

        super.tearDown();
    }

    public File getTempDir()
    {
        return tempDir;
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

    protected abstract String getCheckpointFilename();
}
