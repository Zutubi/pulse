package com.zutubi.pulse.core.scm.cvs;

import com.zutubi.pulse.core.scm.api.ScmException;

import java.io.IOException;

public class CvsClient_1_11_17_IntegrationTest extends AbstractCvsClientIntegrationTestCase
{
    protected CvsClient getClient() throws IOException
    {
        return new CvsClient(":ext:cvs-1.11.17@zutubi.com:/cvsroots/cvs-1.11.17", "integration-test", getPassword("cvs-1.11.17"), null);
    }
}