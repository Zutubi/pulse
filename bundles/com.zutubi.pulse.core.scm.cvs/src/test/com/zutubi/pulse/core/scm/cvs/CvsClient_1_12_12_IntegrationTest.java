package com.zutubi.pulse.core.scm.cvs;

import com.zutubi.pulse.core.scm.api.ScmException;

import java.io.IOException;

public class CvsClient_1_12_12_IntegrationTest extends AbstractCvsClientIntegrationTestCase
{
    protected CvsClient getClient() throws IOException
    {
        return new CvsClient(":ext:cvs-1.12.12@zutubi.com:/cvsroots/cvs-1.12.12", "integration-test", getPassword("cvs-1.12.12"), null);
    }
}