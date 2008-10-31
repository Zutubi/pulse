package com.zutubi.pulse.core.scm.cvs;

import com.zutubi.pulse.core.scm.api.ScmException;

/**
 *
 *
 */
public class Cvs_1_11_19_ClientIntegrationTest extends AbstractCvsClientIntegrationTestCase
{
    protected static final String CVSROOT = ":ext:cvstester@cinnamonbob.com:/cvsroot";
    protected static final String MODULE = "integration-test";

    protected CvsClient getClient()
    {
        return new CvsClient(CVSROOT, MODULE, "cvs", null);
    }

    public void testBrowse() throws ScmException
    {
        // browsing is not currently supported by this version of cvs, to disable the test.
    }

    public void testAttemptingToBrowseFile() throws ScmException
    {
        // browsing is not currently supported by this version of cvs, to disable the test.
    }
}
