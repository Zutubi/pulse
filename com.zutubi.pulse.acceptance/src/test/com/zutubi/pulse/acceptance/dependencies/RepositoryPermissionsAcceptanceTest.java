package com.zutubi.pulse.acceptance.dependencies;

import com.zutubi.pulse.acceptance.BaseXmlRpcAcceptanceTest;
import com.zutubi.pulse.acceptance.Constants;
import static com.zutubi.pulse.acceptance.Constants.Settings.Repository.READ_ACCESS;
import static com.zutubi.pulse.acceptance.Constants.Settings.Repository.WRITE_ACCESS;
import static com.zutubi.pulse.acceptance.dependencies.ArtifactRepositoryTestUtils.getArtifactRepository;
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

public class RepositoryPermissionsAcceptanceTest extends BaseXmlRpcAcceptanceTest
{
    private static final boolean DEBUG = false;

    private String projectName;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();

        projectName = randomName();

        xmlRpcHelper.loginAsAdmin();
        xmlRpcHelper.insertSimpleProject(projectName);
        xmlRpcHelper.logout();

        resetDefaultAccess();

        if (DEBUG)
        {
            System.setProperty("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.SimpleLog");
            System.setProperty("org.apache.commons.logging.simplelog.showdatetime", "true");
            System.setProperty("org.apache.commons.logging.simplelog.log.httpclient.wire.header", "debug");
            System.setProperty("org.apache.commons.logging.simplelog.log.org.apache.commons.httpclient", "debug");
        }
    }

    /*

    tests:
    - check default access permissions
    -- anonymous access disabled.
    -- users can read
    -- admins can write

    - check variations of the configuration.
    -- check anonymous access to a project
     */

    public void testProject_AdminAccess() throws IOException
    {
        UsernamePasswordCredentials credentials = new UsernamePasswordCredentials("admin", "admin");

        ensurePathExists(projectName);
        assertEquals(__200_OK, httpGet(projectName, credentials));
        assertEquals(__201_Created, httpPut(projectName + "/file.txt", credentials));
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
        // remove the anonymous group permissions from this project.
/* Need to fix mismatch between ROLE_GUEST and ROLE_ANONYMOUS */
        ensurePathExists(projectName);
        assertEquals(__200_OK, httpGet(projectName, null));
        assertEquals(__401_Unauthorized, httpPut(projectName + "/file.txt", null));
    }

    public void testDefault_AdminAccess() throws IOException
    {
        UsernamePasswordCredentials credentials = new UsernamePasswordCredentials("admin", "admin");

        String path = "some/" + randomName() + "/path";
        ensurePathExists(path);
        assertEquals(__200_OK, httpGet(path, credentials));
        assertEquals(__201_Created, httpPut(path + "/file.txt", credentials));
    }

    public void testDefault_UserAccess() throws Exception
    {
        Credentials credentials = createRandomUserCredentials();

        String path = "some/" + randomName() + "/path";
        ensurePathExists(path);
        assertEquals(__403_Forbidden, httpGet(path, credentials));
        assertEquals(__403_Forbidden, httpPut(path + "/file.txt", credentials));

        // give users default read write access.
        addReadAccess("groups/all users");
        addWriteAccess("groups/all users");

        assertEquals(__200_OK, httpGet(path, credentials));
        assertEquals(__201_Created, httpPut(path + "/file.txt", credentials));
    }

    public void testDefault_AnonymousAccess() throws Exception
    {
        String path = "some/" + randomName() + "/path";
        ensurePathExists(path);
        assertEquals(__401_Unauthorized, httpGet(path, null));
        assertEquals(__401_Unauthorized, httpPut(path + "/file.txt", null));

        // give anonymous users default read access.
        addReadAccess("groups/anonymous users");

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
        String configPath = Constants.Settings.Repository.PATH;
        
        xmlRpcHelper.loginAsAdmin();
        Hashtable<String, Object> config = xmlRpcHelper.getConfig(configPath);
        @SuppressWarnings("unchecked")
        Vector<String> g = (Vector<String>) config.get(accessList);
        g.addAll(asList(paths));
        xmlRpcHelper.saveConfig(configPath, config, true);
        xmlRpcHelper.logout();
    }

    private void resetDefaultAccess() throws Exception
    {
        String path = Constants.Settings.Repository.PATH;

        xmlRpcHelper.loginAsAdmin();
        Hashtable<String, Object> config = xmlRpcHelper.getConfig(path);
        config.remove(READ_ACCESS);
        config.remove(WRITE_ACCESS);
        xmlRpcHelper.saveConfig(path, config, true);
        xmlRpcHelper.logout();
    }

    private void ensurePathExists(String path) throws IOException
    {
        File root = getArtifactRepository();
        File repositoryPath = new File(root, path);
        if (!repositoryPath.exists() && !repositoryPath.mkdirs())
        {
            throw new IOException("Failed to create path: " + path);
        }
    }

    private int httpGet(String path, Credentials credentials) throws IOException
    {
        // try the path and check the reponse code for forbidden
        HttpClient client = createHttpClient();

        if (credentials != null)
        {
            client.getState().setCredentials(AuthScope.ANY, credentials);
        }

        GetMethod method = new GetMethod(baseUrl + "repository/" + path);
        method.getParams().setCookiePolicy(CookiePolicy.IGNORE_COOKIES);
        client.executeMethod(method);
        method.releaseConnection();

        debug(method);

        return method.getStatusCode();
    }

    private int httpPut(String path, Credentials credentials) throws IOException
    {
        HttpClient client = createHttpClient();

        if (credentials != null)
        {
            client.getState().setCredentials(AuthScope.ANY, credentials);
        }

        PutMethod method = new PutMethod(baseUrl + "repository/" + path);
        method.getParams().setCookiePolicy(CookiePolicy.IGNORE_COOKIES);
        client.executeMethod(method);
        method.releaseConnection();

        debug(method);

        return method.getStatusCode();
    }

    private Credentials createRandomUserCredentials() throws Exception
    {
        String user = randomName();

        xmlRpcHelper.loginAsAdmin();
        xmlRpcHelper.insertTrivialUser(user);
        xmlRpcHelper.logout();

        return new UsernamePasswordCredentials(user, "");
    }

    private HttpClient createHttpClient()
    {
        HttpClient client = new HttpClient();
        client.getHttpConnectionManager().getParams().setConnectionTimeout(5000);
        return client;
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
