/* Copyright 2017 Zutubi Pty Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.zutubi.pulse.acceptance;

import com.zutubi.pulse.master.security.api.ApiPreAuthenticatedProcessingFilter;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.eclipse.jetty.http.HttpStatus;

import java.io.IOException;
import java.util.Hashtable;
import java.util.Vector;

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

        assertEquals(HttpStatus.OK_200, httpGet(getBuildArtifactUrl(random), token));
    }

    public void testApiTokenBecomesInvalidOnApiLogout() throws Exception
    {
        String token = rpcClient.loginAsAdmin();

        rpcClient.RemoteApi.insertSimpleProject(random);
        rpcClient.RemoteApi.triggerBuild(random);
        rpcClient.RemoteApi.waitForBuildToComplete(random, 1);
        String path = getBuildArtifactUrl(random);

        rpcClient.logout();

        assertEquals(HttpStatus.UNAUTHORIZED_401, httpGet(path, token));
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
        assertEquals(HttpStatus.OK_200, httpGet(path, token));
    }

    // sanity check to ensure that we can not access the builds env.txt file without
    // some form of authentication.
    public void testAccessibilityWithoutToken() throws Exception
    {
        rpcClient.loginAsAdmin();

        rpcClient.RemoteApi.insertSimpleProject(random);
        rpcClient.RemoteApi.triggerBuild(random);
        rpcClient.RemoteApi.waitForBuildToComplete(random, 1);

        assertEquals(HttpStatus.UNAUTHORIZED_401, httpGet(getBuildArtifactUrl(random), null));
    }

    public void testAdminAjaxAccess() throws Exception
    {
        String adminAjaxUrl = baseUrl + "/ajax/admin/allPlugins.action";
        assertEquals(HttpStatus.FORBIDDEN_403, httpGet(adminAjaxUrl, null));
        assertEquals(HttpStatus.FORBIDDEN_403, httpGet(adminAjaxUrl, loginRegularUser()));
        assertEquals(HttpStatus.OK_200, httpGet(adminAjaxUrl, rpcClient.loginAsAdmin()));
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

            if (get.getStatusCode() == HttpStatus.OK_200 && get.getPath().contains("login"))
            {
                // we have been redirected to the login page
                return HttpStatus.UNAUTHORIZED_401;
            }
            return get.getStatusCode();
        }
        finally
        {
            get.releaseConnection();
        }
    }
}
