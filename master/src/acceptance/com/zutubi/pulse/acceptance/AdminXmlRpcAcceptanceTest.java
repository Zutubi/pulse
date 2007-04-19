package com.zutubi.pulse.acceptance;

import com.zutubi.util.RandomUtils;
import org.apache.xmlrpc.XmlRpcException;

import java.io.IOException;
import java.util.Hashtable;

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

    public void testCreateUser() throws IOException, XmlRpcException
    {
        Hashtable<String, Object> userDetails = randomUserDetails();

        Object result = xmlRpcClient.execute("RemoteApi.createUser", getVector(adminToken, userDetails));
        assertEquals(Boolean.TRUE, result);
    }

    public void testDeleteUser() throws IOException, XmlRpcException
    {
        Hashtable<String, Object> userDetails = randomUserDetails();

        Object result = xmlRpcClient.execute("RemoteApi.createUser", getVector(adminToken, userDetails));
        assertEquals(Boolean.TRUE, result);

        result = xmlRpcClient.execute("RemoteApi.deleteUser", getVector(adminToken, userDetails.get("login")));
        assertEquals(Boolean.TRUE, result);
    }

    public void testSetPassword() throws XmlRpcException, IOException
    {
        Hashtable<String, Object> userDetails = randomUserDetails();

        Object result = xmlRpcClient.execute("RemoteApi.createUser", getVector(adminToken, userDetails));
        assertEquals(Boolean.TRUE, result);

        String login = (String) userDetails.get("login");

        xmlRpcClient.execute("RemoteApi.setPassword", getVector(adminToken, login, "newPassword"));
        xmlRpcClient.execute("RemoteApi.logout", getVector(adminToken));

        // verify that we can still log in.
        String userToken = (String) xmlRpcClient.execute("RemoteApi.login", getVector(login, "newPassword"));
        assertNotNull(userToken);
    }

    private Hashtable<String, Object> randomUserDetails()
    {
        String login = String.format("user-%s", RandomUtils.randomString(5));
        Hashtable<String, Object> userDetails = new Hashtable<String, Object>();
        userDetails.put("login", login);
        userDetails.put("name", login);
        userDetails.put("password", login);
        return userDetails;
    }
}
