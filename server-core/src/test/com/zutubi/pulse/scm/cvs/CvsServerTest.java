/********************************************************************************
 @COPYRIGHT@
 ********************************************************************************/
/**
 * <class-comment/>
 */
package com.zutubi.pulse.scm.cvs;

import com.zutubi.pulse.util.FileSystemUtils;
import com.zutubi.pulse.filesystem.remote.RemoteFile;
import com.zutubi.pulse.scm.SCMException;
import com.zutubi.pulse.test.PulseTestCase;
import org.netbeans.lib.cvsclient.util.Logger;

import java.io.File;
import java.util.List;

public class CvsServerTest extends PulseTestCase
{
    private String cvsRoot = null;
    private File workdir = null;

    public CvsServerTest(String testName)
    {
        super(testName);
    }

    public void setUp() throws Exception
    {
        super.setUp();

        Logger.setLogging("system");

        // test repository root.
        File repositoryRoot = new File(getPulseRoot(), "server-core/src/test/com/zutubi/pulse/scm/cvs/repository");
        cvsRoot = ":local:" + repositoryRoot.getCanonicalPath();

        // cleanup the working directory.
        workdir = FileSystemUtils.createTempDirectory("CvsServer", "Test");
    }

    public void tearDown() throws Exception
    {
        // add tear down code here.
        removeDirectory(workdir);

        cvsRoot = null;

        super.tearDown();
    }

    public void testListRoot() throws SCMException
    {
        CvsServer cvsServer = new CvsServer(cvsRoot, "unit-test", null, null);
        List<RemoteFile> files = cvsServer.getListing("");
        assertEquals(1, files.size());
        assertEquals("unit-test", files.get(0).getPath());
        assertTrue(files.get(0).isDirectory());
    }

    public void testListing() throws SCMException
    {
        CvsServer cvsServer = new CvsServer(cvsRoot, "unit-test", null, null);
        List<RemoteFile> files = cvsServer.getListing("unit-test/CvsWorkerTest/testRlog");
        assertEquals(4, files.size());

        String [] expectedNames = new String[]{"file1.txt", "Attic", "dir1", "dir2"};
        Boolean [] expectedTypes = new Boolean[]{false, true, true, true};
        for (int i = 0; i < expectedNames.length; i++)
        {
            assertEquals(expectedNames[i], files.get(i).getName());
            assertEquals(expectedTypes[i], Boolean.valueOf(files.get(i).isDirectory()));
        }
    }

    public void testListingNonExistent()
    {
        CvsServer cvsServer = new CvsServer(cvsRoot, "unit-test", null, null);
        try
        {
            cvsServer.getListing("nosuchpath");
            fail();
        }
        catch (SCMException e)
        {
            assertTrue(e.getMessage().contains("does not exist"));
        }

    }

    public void testTestConnection()
    {
        CvsServer cvsServer = new CvsServer(cvsRoot, "unit-test", null, null);
        try
        {
            cvsServer.testConnection();
        }
        catch (SCMException e)
        {
            fail();
        }
    }

    public void testTestConnectionInvalidModule()
    {
        CvsServer cvsServer = new CvsServer(cvsRoot, "some invalid module here", null, null);
        try
        {
            cvsServer.testConnection();
            fail();
        }
        catch (SCMException e)
        {
            assertTrue(e.getMessage().contains("module"));
        }
    }
}