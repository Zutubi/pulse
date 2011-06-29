package com.zutubi.pulse.core.scm.cvs;

import com.zutubi.pulse.core.scm.api.Revision;
import com.zutubi.pulse.core.scm.api.ScmCapability;
import com.zutubi.pulse.core.scm.api.ScmException;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Set;

/**
 * This test case is to test version specific cvs features.
 */
public class CvsClient_1_11_17_Test extends AbstractCvsClientTestCase
{
    private CvsClient client;

    public void setUp() throws Exception
    {
        super.setUp();

        String password = getPassword("cvs-1.11.17");
        assertNotNull(password);

        client = new CvsClient(":ext:cvs-1.11.17@zutubi.com:/cvsroots/cvs-1.11.17", "base", password, null);
    }

    public void testCapabilities()
    {
        Set<ScmCapability> capabilities = client.getCapabilities(scmContext);
        assertFalse(capabilities.contains(ScmCapability.BROWSE));
    }

    public void testAttemptToUpdateToDateOnBranchFails() throws ScmException
    {
        // <author>:<branch/tag>:<date>
        SimpleDateFormat dateFormat = new SimpleDateFormat(CvsRevision.DATE_AND_TIME_FORMAT_STRING);
        Revision rev = new Revision("author:BRANCH:" + dateFormat.format(new Date()));
        try
        {
            client.update(exeContext, rev, null);
            fail();
        }
        catch (ScmException e)
        {
            // expected.
        }
    }

    public void testBrowse() throws ScmException
    {
        // the capabilities say that browsing is not supported, hence we should
        // not be making this call.  The cvs connection will simply hang.
/*
        List<ScmFile> listing = client.browse(scmContext, "base", Revision.HEAD);
        assertEquals(0, listing.size());
*/
    }

    public void testCheckoutToDateOnBranch() throws Exception
    {
        client.checkout(exeContext, new Revision(":BRANCH:" + localTime("2009-02-11 07:08:00 GMT")), null);
        assertFileExists("base/README_BRANCHED.txt");
        assertFileNotExists("base/sample.txt");

        cleanWorkDir();

        client.checkout(exeContext, new Revision(":BRANCH:" + localTime("2009-02-11 07:08:05 GMT")), null);
        assertFileExists("base/README_BRANCHED.txt");
        assertFileExists("base/sample.txt");
    }

}