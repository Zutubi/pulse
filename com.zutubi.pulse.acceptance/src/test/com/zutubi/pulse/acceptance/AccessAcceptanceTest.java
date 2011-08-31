package com.zutubi.pulse.acceptance;

import com.zutubi.pulse.master.security.api.ApiPreAuthenticatedProcessingFilter;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;

import java.io.IOException;
import java.util.Hashtable;
import java.util.Vector;

import static org.mortbay.http.HttpResponse.*;

/**
 * A general bucket for user access and authentication related acceptance
 * tests.
 */
public class AccessAcceptanceTest extends AcceptanceTestBase
{
    public void testApiTokenCanBeUsedToAccessWebUi() throws Exception
    {
        String token = rpcClient.loginAsAdmin();

        rpcClient.RemoteApi.insertSimpleProject(random);
        rpcClient.RemoteApi.triggerBuild(random);
        rpcClient.RemoteApi.waitForBuildToComplete(random, 1);

        assertEquals(__200_OK, httpGet(getBuildArtifactUrl(random), token));
    }

    public void testApiTokenBecomesInvalidOnApiLogout() throws Exception
    {
        String token = rpcClient.loginAsAdmin();

        rpcClient.RemoteApi.insertSimpleProject(random);
        rpcClient.RemoteApi.triggerBuild(random);
        rpcClient.RemoteApi.waitForBuildToComplete(random, 1);
        String path = getBuildArtifactUrl(random);

        rpcClient.logout();

        assertEquals(__401_Unauthorized, httpGet(path, token));
    }

    public void testApiTokenForNonAdminUser() throws Exception
    {
        rpcClient.loginAsAdmin();
        rpcClient.RemoteApi.insertSimpleProject(random);
        rpcClient.RemoteApi.triggerBuild(random);
        rpcClient.RemoteApi.waitForBuildToComplete(random, 1);
        String path = getBuildArtifactUrl(random);
        rpcClient.logout();

        String token = loginRegularUser();
        assertEquals(__200_OK, httpGet(path, token));
    }

    // sanity check to ensure that we can not access the builds env.txt file without
    // some form of authentication.
    public void testAccessibilityWithoutToken() throws Exception
    {
        rpcClient.loginAsAdmin();

        rpcClient.RemoteApi.insertSimpleProject(random);
        rpcClient.RemoteApi.triggerBuild(random);
        rpcClient.RemoteApi.waitForBuildToComplete(random, 1);

        assertEquals(__401_Unauthorized, httpGet(getBuildArtifactUrl(random), null));
    }

    public void testAdminAjaxAccess() throws Exception
    {
        String adminAjaxUrl = baseUrl + "/ajax/admin/allPlugins.action";
        assertEquals(__403_Forbidden, httpGet(adminAjaxUrl, null));
        assertEquals(__403_Forbidden, httpGet(adminAjaxUrl, loginRegularUser()));
        assertEquals(__200_OK, httpGet(adminAjaxUrl, rpcClient.loginAsAdmin()));
    }

    private String loginRegularUser() throws Exception
    {
        rpcClient.loginAsAdmin();
        String user = randomName();
        rpcClient.RemoteApi.insertTrivialUser(user);
        rpcClient.logout();

        return rpcClient.login(user, "");
    }

    // get the url for the builds env.txt
    private String getBuildArtifactUrl(String projectName) throws Exception
    {
        Vector<Hashtable<String, Object>> artifacts = rpcClient.RemoteApi.getArtifactsInBuild(projectName, 1);
        return baseUrl + artifacts.get(2).get("permalink").toString() + "env.txt";
    }

    private int httpGet(String path, String token) throws IOException
    {
        GetMethod get = new GetMethod(path);
        try
        {
            if (token != null)
            {
                get.addRequestHeader(ApiPreAuthenticatedProcessingFilter.REQUEST_HEADER, token);
            }

            HttpClient client = new HttpClient();
            client.getHttpConnectionManager().getParams().setConnectionTimeout(5000);
            client.executeMethod(get);

            if (get.getStatusCode() == __200_OK && get.getPath().contains("login"))
            {
                // we have been redirected to the login page
                return __401_Unauthorized;
            }
            return get.getStatusCode();
        }
        finally
        {
            get.releaseConnection();
        }
    }
}
