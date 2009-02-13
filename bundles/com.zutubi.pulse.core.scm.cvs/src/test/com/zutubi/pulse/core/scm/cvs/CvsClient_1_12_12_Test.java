package com.zutubi.pulse.core.scm.cvs;

import com.zutubi.pulse.core.scm.api.ScmCapability;
import com.zutubi.pulse.core.scm.api.ScmException;
import com.zutubi.pulse.core.scm.api.Revision;

import java.util.Set;
import java.text.ParseException;

/**
 * This test case is to test version specific cvs features.
 */
public class CvsClient_1_12_12_Test extends AbstractCvsClientTestCase
{
    private CvsClient client;

    public void setUp() throws Exception
    {
        super.setUp();

        String password = getPassword("cvs-1.12.12");
        assertNotNull(password);
        
        client = new CvsClient(":ext:cvs-1.12.12@zutubi.com:/cvsroots/cvs-1.12.12", "base", password, null);
    }

    public void testCapabilities()
    {
        Set<ScmCapability> capabilities = client.getCapabilities(scmContext);
        assertTrue(capabilities.contains(ScmCapability.BROWSE));
    }

    // 1.12.12 is the first version of cvs to support the -r TAG[:date] revision format.
    public void testUpdateToDateOnBranch() throws ScmException, ParseException
    {
        client.checkout(exeContext, new Revision(":BRANCH:"), null);
        assertFileExists("base/README_BRANCHED.txt");
        assertFileExists("base/sample.txt");

        client.update(exeContext, new Revision(":BRANCH:" + localTime("2009-02-11 07:08:00 GMT")), null);
        assertFileNotExists("base/sample.txt");

        client.update(exeContext, new Revision(":BRANCH:" + localTime("2009-02-11 07:08:05 GMT")), null);
        assertFileExists("base/sample.txt");
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
