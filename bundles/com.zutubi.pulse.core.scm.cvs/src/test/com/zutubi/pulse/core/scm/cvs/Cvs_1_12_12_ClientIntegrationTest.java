package com.zutubi.pulse.core.scm.cvs;

/**
 *
 *
 */
public class Cvs_1_12_12_ClientIntegrationTest extends AbstractCvsClientIntegrationTestCase
{
    protected static final String CVSROOT = ":ext:cvs-1.12.12@cinnamonbob.com:/cvsroots/cvs-1.12.12";
    protected static final String MODULE = "integration-test";

    protected CvsClient getClient()
    {
        return new CvsClient(CVSROOT, MODULE, "cvs-1.12.12", null);
    }
}
