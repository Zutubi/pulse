package com.zutubi.pulse.core.scm.p4;

import com.zutubi.pulse.core.scm.p4.config.PerforceConfiguration;

import java.io.File;

/**
 * Set of tests to verify the ability of the Perforce Client to communicate
 * with a unicode server.
 */
public class PerforceUnicodeTest extends PerforceTestBase
{
    private static final int P4D_PORT = 6666;

    private File workDir;

    private PerforceConfiguration configuration;
    private PerforceClient client;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();

        workDir = new File(tmpDir, "work");

        configuration = new PerforceConfiguration(Integer.toString(P4D_PORT), "test-user", "", "daniel-PC");
    }

    public void testNonUnicodeServerCheckout() throws Exception
    {
        deployPerforceServer("snapshot", P4D_PORT, 1, false);

        client = new PerforceClient(configuration, new PerforceWorkspaceManager());
        client.checkout(createExecutionContext(workDir, false), null, null);

        assertTrue(new File(workDir, "readme.txt").isFile());
        assertTrue(new File(workDir, "doc/readme.txt").isFile());
    }

    public void testUnicodeServerCheckout() throws Exception
    {
        deployPerforceServer("snapshot", P4D_PORT, 1, true);
        configuration.setUnicodeServer(true);
        configuration.setCharset("utf8");

        client = new PerforceClient(configuration, new PerforceWorkspaceManager());
        client.checkout(createExecutionContext(workDir, false), null, null);

        assertTrue(new File(workDir, "readme.txt").isFile());
        assertTrue(new File(workDir, "doc/readme.txt").isFile());
    }
}
