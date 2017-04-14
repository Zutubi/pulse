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

package com.zutubi.pulse.acceptance.rpc;

import com.zutubi.pulse.acceptance.AcceptanceTestUtils;
import static com.zutubi.pulse.acceptance.AcceptanceTestUtils.ADMIN_CREDENTIALS;
import com.zutubi.pulse.core.api.PulseRuntimeException;
import org.apache.xmlrpc.XmlRpcClient;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.Vector;

/**
 * Client used to access various Pulse XML-RPC APIs.  Manages authentication
 * tokens, and provides access to each API via public properties (e.g.
 * {@link #RemoteApi}).
 */
public class RpcClient
{
    /**
     * Client for {@link com.zutubi.pulse.master.api.MonitorApi}.
     */
    public MonitorApiClient MonitorApi;
    /**
     * Client for {@link com.zutubi.pulse.master.api.RemoteApi}.
     */
    public RemoteApiClient RemoteApi;
    /**
     * Client for {@link com.zutubi.pulse.master.api.TestApi}.
     */
    public TestApiClient TestApi;

    private XmlRpcClient xmlRpcClient;
    private String token = null;

    /**
     * Creates a new client for accessing the default testing Pulse master.
     */
    public RpcClient()
    {
        this(AcceptanceTestUtils.getPulseUrl() + "/xmlrpc");
    }

    /**
     * Creates a new client pointing at the given URL.
     *
     * @param url url for the server to talk to
     */
    public RpcClient(String url)
    {
        try
        {
            xmlRpcClient = new XmlRpcClient(new URL(url));
        }
        catch (MalformedURLException e)
        {
            throw new PulseRuntimeException(e);
        }

        MonitorApi = new MonitorApiClient(this);
        RemoteApi = new RemoteApiClient(this);
        TestApi = new TestApiClient(this);
    }

    /**
     * Logs this client in, caching the returned token for use in later calls.
     *
     * @param login    user name
     * @param password user password
     * @return the token returned by logging in
     * @throws Exception on error
     */
    public String login(String login, String password) throws Exception
    {
        token = (String) callWithoutToken(RemoteApiClient.API_NAME, "login", login, password);
        return token;
    }

    /**
     * Logs in as the default admin user, caching the token for use in later
     * calls.
     *
     * @return the token returned by logging in
     * @throws Exception on error
     */
    public String loginAsAdmin() throws Exception
    {
        return login(ADMIN_CREDENTIALS.getUserName(), ADMIN_CREDENTIALS.getPassword());
    }

    /**
     * Logs out, invalidating the current token.
     *
     * @return true
     * @throws Exception on error
     */
    public boolean logout() throws Exception
    {
        verifyLoggedIn();
        Object result = callWithoutToken(RemoteApiClient.API_NAME, "logout", token);
        token = null;
        return (Boolean)result;
    }

    /**
     * Inicates if this client has an authentication token from a prior login.
     *
     * @return true if we have a token
     */
    public boolean isLoggedIn()
    {
        return token != null;
    }

    private void verifyLoggedIn()
    {
        if(!isLoggedIn())
        {
            throw new IllegalStateException("Not logged in, call login first");
        }
    }

    /**
     * Calls the given function on the given API without implicitly passing the
     * current login token as the first argument.
     *
     * @param api      name of the API to call
     * @param function name of the function to call
     * @param args     arguments to pass to the function
     * @param <T> type of the returned object
     * @return the result of the API call
     * @throws Exception on error
     */
    @SuppressWarnings({ "unchecked" })
    public <T> T callWithoutToken(String api, String function, Object... args) throws Exception
    {
        return (T) xmlRpcClient.execute(api + "." + function, new Vector<Object>(Arrays.asList(args)));
    }

    /**
     * Calls the given function on the given API, implicitly passing the
     * current login token as the first argument.
     *
     * @param api      name of the API to call
     * @param function name of the function to call
     * @param args     arguments to pass to the function
     * @param <T> type of the returned object
     * @return the result of the API call
     * @throws Exception on error
     */
    @SuppressWarnings({"unchecked"})
    protected <T> T callApi(String api, String function, Object... args) throws Exception
    {
        verifyLoggedIn();
        Vector<Object> argVector = new Vector<Object>(args.length + 1);
        argVector.add(token);
        argVector.addAll(Arrays.asList(args));
        return (T) xmlRpcClient.execute(api + "." + function, argVector);
    }

    /**
     * A utility method that cancels all queued builds and cancels any builds that are still
     * running.
     *
     * @throws Exception on error.
     */
    public void cancelIncompleteBuilds() throws Exception
    {
        for (Hashtable<String, Object> queuedRequest : RemoteApi.getBuildQueueSnapshot())
        {
            RemoteApi.cancelQueuedBuildRequest(queuedRequest.get("id").toString());
        }

        TestApi.cancelActiveBuilds();
    }

    public static void main(String[] argv) throws Exception
    {
        RpcClient client = new RpcClient();
        client.loginAsAdmin();
        try
        {
            client.RemoteApi.insertSimpleProject(argv[0], false);
        }
        finally
        {
            client.logout();
        }
    }
}
