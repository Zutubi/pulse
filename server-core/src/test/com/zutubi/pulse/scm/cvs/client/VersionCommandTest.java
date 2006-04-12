package com.zutubi.pulse.scm.cvs.client;

import com.zutubi.pulse.scm.SCMException;
import com.zutubi.pulse.scm.cvs.client.CvsClient;
import com.zutubi.pulse.test.BobTestCase;

import java.io.File;

/**
 * <class-comment/>
 */
public class VersionCommandTest extends BobTestCase
{
    private CvsClient client;

    public VersionCommandTest()
    {
    }

    public VersionCommandTest(String name)
    {
        super(name);
    }

    protected void setUp() throws Exception
    {
        super.setUp();

        File repositoryRoot = new File(getBobRoot(), "server-core/src/test/com/zutubi/pulse/scm/cvs/repository");
        String cvsRoot = ":local:" + repositoryRoot.getCanonicalPath();
        client = new CvsClient(cvsRoot);

    }

    protected void tearDown() throws Exception
    {
        super.tearDown();
    }

    public void testVersionCommand() throws SCMException
    {
        VersionCommand command = new VersionCommand();
        client.executeCommand(command);
        String serverVersion = command.getVersion();

        assertNotNull(serverVersion);
        assertTrue(serverVersion + " does not contain 'CVS'.", serverVersion.contains("CVS"));
    }

}
