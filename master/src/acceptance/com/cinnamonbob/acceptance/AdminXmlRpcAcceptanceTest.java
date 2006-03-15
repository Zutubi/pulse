package com.cinnamonbob.acceptance;

import org.apache.xmlrpc.XmlRpcException;

/**
 * <class-comment/>
 */
public class AdminXmlRpcAcceptanceTest extends BaseXmlRpcAcceptanceTest
{
    private String adminToken;

    public AdminXmlRpcAcceptanceTest()
    {
    }

    public AdminXmlRpcAcceptanceTest(String testName)
    {
        super(testName);
    }

    protected void setUp() throws Exception
    {
        super.setUp();

        adminToken = (String) xmlRpcClient.execute("RemoteApi.login", new Object[]{"admin", "admin"});
    }

    protected void tearDown() throws Exception
    {
        adminToken = null;

        super.tearDown();
    }

    public void testSetPassword() throws XmlRpcException
    {
        // we should really change the password on a temporary account to
        // ensure that we dont 'mess' things up for the test of the acceptance
        // tests. However, until we have a REMOTE API for creating users, this
        // will have to do.

        xmlRpcClient.execute("RemoteApi.setPassword", new Object[]{adminToken, "admin", "newPassword"});
        xmlRpcClient.execute("RemoteApi.logout", new Object[]{adminToken});

        // verify that we can still log in.
        adminToken = (String) xmlRpcClient.execute("RemoteApi.login", new Object[]{"admin", "newPassword"});

        // put the password back the way it was.
        xmlRpcClient.execute("RemoteApi.setPassword", new Object[]{adminToken, "admin", "admin"});
        xmlRpcClient.execute("RemoteApi.logout", new Object[]{adminToken});
    }
}
