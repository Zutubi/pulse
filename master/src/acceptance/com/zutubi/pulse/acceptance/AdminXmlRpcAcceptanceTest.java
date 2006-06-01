package com.zutubi.pulse.acceptance;

import org.apache.xmlrpc.XmlRpcException;

import java.util.Vector;
import java.util.Arrays;
import java.io.IOException;

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

        adminToken = (String) xmlRpcClient.execute("RemoteApi.login", getVector("admin", "admin"));
    }

    protected void tearDown() throws Exception
    {
        adminToken = null;

        super.tearDown();
    }

    public void testSetPassword() throws XmlRpcException, IOException
    {
        // we should really change the password on a temporary account to
        // ensure that we dont 'mess' things up for the test of the acceptance
        // tests. However, until we have a REMOTE API for creating users, this
        // will have to do.

        xmlRpcClient.execute("RemoteApi.setPassword", getVector(adminToken, "admin", "newPassword"));
        xmlRpcClient.execute("RemoteApi.logout", getVector(adminToken));

        // verify that we can still log in.
        adminToken = (String) xmlRpcClient.execute("RemoteApi.login", getVector("admin", "newPassword"));

        // put the password back the way it was.
        xmlRpcClient.execute("RemoteApi.setPassword", getVector(adminToken, "admin", "admin"));
        xmlRpcClient.execute("RemoteApi.logout", getVector(adminToken));
    }
}
