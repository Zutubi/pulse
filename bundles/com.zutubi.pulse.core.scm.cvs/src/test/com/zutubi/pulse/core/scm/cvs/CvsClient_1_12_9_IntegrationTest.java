package com.zutubi.pulse.core.scm.cvs;

import com.zutubi.pulse.core.scm.api.ScmException;

import java.io.IOException;

public class CvsClient_1_12_9_IntegrationTest extends AbstractCvsClientIntegrationTestCase
{
    protected CvsClient getClient() throws IOException
    {
        return new CvsClient(":ext:cvs-1.12.9@zutubi.com:/cvsroots/default", "integration-test", getPassword("cvs-1.12.9"), null);
    }

    public void testAttemptingToBrowseFile() throws ScmException
    {
        try
        {
            super.testAttemptingToBrowseFile();
            fail();
        }
        catch (ScmException e)
        {
            // expected.
        }
    }
}
