package com.zutubi.pulse.core.scm.cvs.client.commands;

import junit.framework.TestCase;
import org.netbeans.lib.cvsclient.CVSRoot;
import org.netbeans.lib.cvsclient.util.Logger;

import java.util.List;

import com.zutubi.pulse.core.scm.cvs.client.CvsCore;
import com.zutubi.pulse.core.scm.ScmException;

/**
 *
 *
 */
public class RlsCommandTest extends TestCase
{
    private CvsCore core;

    protected void setUp() throws Exception
    {
        super.setUp();

        Logger.setLogging("system");

        core = new CvsCore();
        core.setRoot(CVSRoot.parse(":ext:cvs-1.12.12@cinnamonbob.com:/cvsroots/cvs-1.12.12"));
        core.setPassword("cvs-1.12.12");
    }

    protected void tearDown() throws Exception
    {
        super.tearDown();
    }

    public void testRlsCommandOnProject() throws ScmException
    {
        RlsCommand rls = new RlsCommand();
        rls.setPaths("integration-test/project");
        rls.setDisplayInEntriesFormat(true); // required for full details...
        core.executeCommand(rls, null, null);

        List<RlsInfo> listing = rls.getListing();
        assertEquals(4, listing.size());
        assertTrue(listing.contains(new RlsInfo("integration-test/project", "README.txt", false)));
        assertTrue(listing.contains(new RlsInfo("integration-test/project", "build.xml", false)));
        assertTrue(listing.contains(new RlsInfo("integration-test/project", "src", true)));
        assertTrue(listing.contains(new RlsInfo("integration-test/project", "test", true)));
    }

    public void testRlsCommandOnProjectSrc() throws ScmException
    {
        RlsCommand rls = new RlsCommand();
        rls.setPaths("integration-test/project/src");
        rls.setDisplayInEntriesFormat(true);
        core.executeCommand(rls, null, null);

        List<RlsInfo> listing = rls.getListing();
        assertEquals(1, listing.size());
        assertTrue(listing.contains(new RlsInfo("integration-test/project/src", "com", true)));
    }

    public void testRlsCommandOnProjectSrcCom() throws ScmException
    {
        RlsCommand rls = new RlsCommand();
        rls.setPaths("integration-test/project/src/com");
        rls.setDisplayInEntriesFormat(true);
        core.executeCommand(rls, null, null);

        List<RlsInfo> listing = rls.getListing();
        assertEquals(2, listing.size());
        assertTrue(listing.contains(new RlsInfo("integration-test/project/src/com", "package.properties", false)));
        assertTrue(listing.contains(new RlsInfo("integration-test/project/src/com", "Com.java", false)));
    }

    public void testRlsCommandOnFile() throws ScmException
    {
        RlsCommand rls = new RlsCommand();
        rls.setPaths("integration-test/project/test/Test.java");
        rls.setDisplayInEntriesFormat(true);
        core.executeCommand(rls, null, null);

        List<RlsInfo> listing = rls.getListing();
        assertEquals(1, listing.size());
        assertTrue(listing.contains(new RlsInfo("integration-test/project/test", "Test.java", false)));
    }

    public void testRlsCommandWithoutPath() throws ScmException
    {
        RlsCommand rls = new RlsCommand();
        rls.setDisplayInEntriesFormat(true);
        core.executeCommand(rls, null, null);

        List<RlsInfo> listing = rls.getListing();
        assertTrue(listing.contains(new RlsInfo(null, "CVSROOT", true)));
    }

    public void testRlsCommandOnEmptyDirectory() throws ScmException
    {
        // we currently do not have an empty directory in the test data.
    }
}

