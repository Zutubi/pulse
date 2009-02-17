package com.zutubi.pulse.core.scm.cvs;

import com.zutubi.pulse.core.scm.api.Revision;
import com.zutubi.pulse.core.scm.api.ScmCapability;
import com.zutubi.pulse.core.scm.api.ScmException;

import java.text.ParseException;
import java.util.Set;

/**
 * This test case is to test version specific cvs features.
 */
public class CvsClient_1_12_9_Test extends AbstractCvsClientTestCase
{
    private CvsClient client;

    public void setUp() throws Exception
    {
        super.setUp();

        String password = getPassword("cvs-1.12.9");
        assertNotNull(password);

        client = new CvsClient(":ext:cvs-1.12.9@zutubi.com:/cvsroots/default", "base", password, null);
    }

    public void testCapabilities()
    {
        Set<ScmCapability> capabilities = client.getCapabilities(scmContext);
        assertTrue(capabilities.contains(ScmCapability.BROWSE));
    }

    // 1.12.12 is the first version of cvs to support the -r TAG[:date] revision format.
    public void testUpdateToDateOnBranch() throws ScmException
    {
        try
        {
            Revision rev = new Revision(":BRANCH:20090211-07:08:00");
            client.checkout(exeContext, rev, null);
            client.update(exeContext, rev, null);
            fail();
        }
        catch (ScmException e)
        {
            // expected.
        }
    }

    public void testUpdateToBranch() throws ScmException
    {
        Revision rev = new Revision(":BRANCH:");
        client.checkout(exeContext, rev, null);
        client.update(exeContext, rev, null);
        
        assertFileExists("base/README_BRANCHED.txt");
        assertFileExists("base/sample.txt");
    }

    public void testCheckoutToDateOnBranch() throws ScmException, ParseException
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