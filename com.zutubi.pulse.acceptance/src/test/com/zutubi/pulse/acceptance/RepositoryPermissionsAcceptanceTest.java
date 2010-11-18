package com.zutubi.pulse.acceptance;

import static com.zutubi.pulse.acceptance.AcceptanceTestUtils.ADMIN_CREDENTIALS;
import static com.zutubi.pulse.acceptance.Constants.Settings.Repository.READ_ACCESS;
import static com.zutubi.pulse.acceptance.Constants.Settings.Repository.WRITE_ACCESS;
import com.zutubi.pulse.acceptance.utils.Repository;
import com.zutubi.pulse.core.dependency.RepositoryAttributes;
import static com.zutubi.pulse.master.model.UserManager.ALL_USERS_GROUP_NAME;
import static com.zutubi.pulse.master.model.UserManager.ANONYMOUS_USERS_GROUP_NAME;
import static com.zutubi.pulse.master.tove.config.MasterConfigurationRegistry.GROUPS_SCOPE;
import org.apache.commons.httpclient.*;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PutMethod;
import static org.mortbay.http.HttpResponse.*;

import java.io.File;
import java.io.IOException;
import static java.util.Arrays.asList;
import java.util.Hashtable;
import java.util.Vector;

public class RepositoryPermissionsAcceptanceTest extends AcceptanceTestBase
{
    private static final boolean DEBUG = false;

    private String projectName;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();

        projectName = randomName();

        rpcClient.loginAsAdmin();
        String projectPath = rpcClient.RemoteApi.insertSimpleProject(projectName);
        String projectHandle = rpcClient.RemoteApi.getConfigHandle(projectPath);
        rpcClient.logout();

        // since we are not running builds as such, we need to initialise the repository by manually adding
        // the project handle to the appropriate path.  This is normally handled during a build.
        RepositoryAttributes attributes = new RepositoryAttributes(Repository.getRepositoryBase());
        attributes.addAttribute(projectName, RepositoryAttributes.PROJECT_HANDLE, projectHandle);

        resetDefaultAccess();

        if (DEBUG)
        {
            System.setProperty("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.SimpleLog");
            System.setProperty("org.apache.commons.logging.simplelog.showdatetime", "true");
            System.setProperty("org.apache.commons.logging.simplelog.log.httpclient.wire.header", "debug");
            System.setProperty("org.apache.commons.logging.simplelog.log.org.apache.commons.httpclient", "debug");
        }
    }

    public void testProject_AdminAccess() throws IOException
    {
        ensurePathExists(projectName);
        assertEquals(__200_OK, httpGet(projectName, ADMIN_CREDENTIALS));
        assertEquals(__201_Created, httpPut(projectName + "/file.txt", ADMIN_CREDENTIALS));
    }

    public void testProject_UserAccess() throws Exception
    {
        Credentials credentials = createRandomUserCredentials();

        ensurePathExists(projectName);
        assertEquals(__200_OK, httpGet(projectName, credentials));
        assertEquals(__403_Forbidden, httpPut(projectName + "/file.txt", credentials));
    }

    public void testProject_AnonymousAccess() throws IOException
    {
        ensurePathExists(projectName);
        assertEquals(__200_OK, httpGet(projectName, null));
        assertEquals(__401_Unauthorized, httpPut(projectName + "/file.txt", null));
    }

    public void testDefault_AdminAccess() throws IOException
    {
        String path = "some/" + randomName() + "/path";
        ensurePathExists(path);
        assertEquals(__200_OK, httpGet(path, ADMIN_CREDENTIALS));
        assertEquals(__201_Created, httpPut(path + "/file.txt", ADMIN_CREDENTIALS));
    }

    public void testDefault_UserAccess() throws Exception
    {
        Credentials credentials = createRandomUserCredentials();

        String path = "some/" + randomName() + "/path";
        ensurePathExists(path);
        assertEquals(__403_Forbidden, httpGet(path, credentials));
        assertEquals(__403_Forbidden, httpPut(path + "/file.txt", credentials));

        // give users default read write access.
        addReadAccess(getGroupPath(ALL_USERS_GROUP_NAME));
        addWriteAccess(getGroupPath(ALL_USERS_GROUP_NAME));

        assertEquals(__200_OK, httpGet(path, credentials));
        assertEquals(__201_Created, httpPut(path + "/file.txt", credentials));
    }

    private String getGroupPath(String name)
    {
        return GROUPS_SCOPE + "/" + name;
    }

    public void testDefault_AnonymousAccess() throws Exception
    {
        String path = "some/" + randomName() + "/path";
        ensurePathExists(path);
        assertEquals(__401_Unauthorized, httpGet(path, null));
        assertEquals(__401_Unauthorized, httpPut(path + "/file.txt", null));

        // give anonymous users default read access.
        addReadAccess(getGroupPath(ANONYMOUS_USERS_GROUP_NAME));

        assertEquals(__200_OK, httpGet(path, null));
        assertEquals(__401_Unauthorized, httpPut(path + "/file.txt", null));
    }

    private void addReadAccess(String... groupPaths) throws Exception
    {
        addAccess(READ_ACCESS, groupPaths);
    }

    private void addWriteAccess(String... groupPaths) throws Exception
    {
        addAccess(WRITE_ACCESS, groupPaths);
    }

    private void addAccess(String accessList, String... paths) throws Exception
    {
        rpcClient.loginAsAdmin();
        Hashtable<String, Object> config = rpcClient.RemoteApi.getConfig(Constants.Settings.Repository.PATH);
        @SuppressWarnings("unchecked")
        Vector<String> g = (Vector<String>) config.get(accessList);
        g.addAll(asList(paths));
        rpcClient.RemoteApi.saveConfig(Constants.Settings.Repository.PATH, config, true);
        rpcClient.logout();
    }

    private void resetDefaultAccess() throws Exception
    {
        rpcClient.loginAsAdmin();
        Hashtable<String, Object> config = rpcClient.RemoteApi.getConfig(Constants.Settings.Repository.PATH);
        config.remove(READ_ACCESS);
        config.remove(WRITE_ACCESS);
        rpcClient.RemoteApi.saveConfig(Constants.Settings.Repository.PATH, config, true);
        rpcClient.logout();
    }

    private void ensurePathExists(String path) throws IOException
    {
        File root = Repository.getRepositoryBase();
        File repositoryPath = new File(root, path);
        if (!repositoryPath.exists() && !repositoryPath.mkdirs())
        {
            throw new IOException("Failed to create path: " + path);
        }
    }

    private int httpGet(String path, Credentials credentials) throws IOException
    {
        GetMethod method = new GetMethod(baseUrl + "/repository/" + path);
        method.getParams().setCookiePolicy(CookiePolicy.IGNORE_COOKIES);
        return executeMethod(method, credentials);
    }

    private int httpPut(String path, Credentials credentials) throws IOException
    {
        PutMethod method = new PutMethod(baseUrl + "/repository/" + path);
        method.getParams().setCookiePolicy(CookiePolicy.IGNORE_COOKIES);
        return executeMethod(method, credentials);
    }

    private Credentials createRandomUserCredentials() throws Exception
    {
        String user = randomName();

        rpcClient.loginAsAdmin();
        rpcClient.RemoteApi.insertTrivialUser(user);
        rpcClient.logout();

        return new UsernamePasswordCredentials(user, "");
    }

    private int executeMethod(HttpMethod method, Credentials credentials) throws IOException
    {
        HttpClient client = new HttpClient();
        client.getHttpConnectionManager().getParams().setConnectionTimeout(5000);
        if (credentials != null)
        {
            client.getState().setCredentials(AuthScope.ANY, credentials);
        }

        client.executeMethod(method);
        method.releaseConnection();

        debug(method);

        return method.getStatusCode();
    }

    private void debug(HttpMethod method) throws IOException
    {
        if (DEBUG)
        {
            System.out.println("Status Code: " + method.getStatusCode());

            //write out the request headers
            System.out.println("*** Request ***");
            System.out.println("Request Path: " + method.getPath());
            System.out.println("Request Query: " + method.getQueryString());
            Header[] requestHeaders = method.getRequestHeaders();
            for (Header requestHeader : requestHeaders)
            {
                System.out.print(requestHeader);
            }

            //write out the response headers
            System.out.println("*** Response ***");
            System.out.println("Status Line: " + method.getStatusLine());
            Header[] responseHeaders = method.getResponseHeaders();
            for (Header responseHeader : responseHeaders)
            {
                System.out.print(responseHeader);
            }

            //write out the response body
            System.out.println("*** Response Body ***");
            System.out.println(new String(method.getResponseBody()));
        }
    }
}
