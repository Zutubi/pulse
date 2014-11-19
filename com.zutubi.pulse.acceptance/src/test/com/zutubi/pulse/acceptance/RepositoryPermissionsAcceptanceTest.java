package com.zutubi.pulse.acceptance;

import com.zutubi.pulse.acceptance.utils.Repository;
import com.zutubi.pulse.core.dependency.RepositoryAttributes;
import org.apache.commons.httpclient.*;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PutMethod;

import java.io.File;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Vector;

import static com.zutubi.pulse.acceptance.AcceptanceTestUtils.ADMIN_CREDENTIALS;
import static com.zutubi.pulse.acceptance.Constants.Settings.Repository.READ_ACCESS;
import static com.zutubi.pulse.acceptance.Constants.Settings.Repository.WRITE_ACCESS;
import static com.zutubi.pulse.master.model.UserManager.ALL_USERS_GROUP_NAME;
import static com.zutubi.pulse.master.model.UserManager.ANONYMOUS_USERS_GROUP_NAME;
import static com.zutubi.pulse.master.tove.config.MasterConfigurationRegistry.GROUPS_SCOPE;
import static java.util.Arrays.asList;

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
        assertEquals(HttpStatus.SC_OK, httpGet(projectName, ADMIN_CREDENTIALS));
        assertEquals(HttpStatus.SC_CREATED, httpPut(projectName + "/file.txt", ADMIN_CREDENTIALS));
    }

    public void testProject_UserAccess() throws Exception
    {
        Credentials credentials = createRandomUserCredentials();

        ensurePathExists(projectName);
        assertEquals(HttpStatus.SC_OK, httpGet(projectName, credentials));
        assertEquals(HttpStatus.SC_FORBIDDEN, httpPut(projectName + "/file.txt", credentials));
    }

    public void testProject_AnonymousAccess() throws IOException
    {
        ensurePathExists(projectName);
        assertEquals(HttpStatus.SC_OK, httpGet(projectName, null));
        assertEquals(HttpStatus.SC_UNAUTHORIZED, httpPut(projectName + "/file.txt", null));
    }

    public void testDefault_AdminAccess() throws IOException
    {
        String path = "some/" + randomName() + "/path";
        ensurePathExists(path);
        assertEquals(HttpStatus.SC_OK, httpGet(path, ADMIN_CREDENTIALS));
        assertEquals(HttpStatus.SC_CREATED, httpPut(path + "/file.txt", ADMIN_CREDENTIALS));
    }

    public void testDefault_UserAccess() throws Exception
    {
        Credentials credentials = createRandomUserCredentials();

        String path = "some/" + randomName() + "/path";
        ensurePathExists(path);
        assertEquals(HttpStatus.SC_FORBIDDEN, httpGet(path, credentials));
        assertEquals(HttpStatus.SC_FORBIDDEN, httpPut(path + "/file.txt", credentials));

        // give users default read write access.
        addReadAccess(getGroupPath(ALL_USERS_GROUP_NAME));
        addWriteAccess(getGroupPath(ALL_USERS_GROUP_NAME));

        assertEquals(HttpStatus.SC_OK, httpGet(path, credentials));
        assertEquals(HttpStatus.SC_CREATED, httpPut(path + "/file.txt", credentials));
    }

    private String getGroupPath(String name)
    {
        return GROUPS_SCOPE + "/" + name;
    }

    public void testDefault_AnonymousAccess() throws Exception
    {
        String path = "some/" + randomName() + "/path";
        ensurePathExists(path);
        assertEquals(HttpStatus.SC_UNAUTHORIZED, httpGet(path, null));
        assertEquals(HttpStatus.SC_UNAUTHORIZED, httpPut(path + "/file.txt", null));

        // give anonymous users default read access.
        addReadAccess(getGroupPath(ANONYMOUS_USERS_GROUP_NAME));

        assertEquals(HttpStatus.SC_OK, httpGet(path, null));
        assertEquals(HttpStatus.SC_UNAUTHORIZED, httpPut(path + "/file.txt", null));
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

        if (DEBUG)
        {
            AcceptanceTestUtils.debug(method);
        }

        AcceptanceTestUtils.releaseConnection(method);

        return method.getStatusCode();
    }
}
