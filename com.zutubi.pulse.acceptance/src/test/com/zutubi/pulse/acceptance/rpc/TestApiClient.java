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

import java.util.Hashtable;
import java.util.Vector;

/**
 * An XML-RPC client for {@link com.zutubi.pulse.master.api.TestApi}.
 */
public class TestApiClient extends ApiClient
{
    public static final String API_NAME = "TestApi";

    public TestApiClient(RpcClient rpc)
    {
        super(API_NAME, rpc);
    }

    public void logError(String message) throws Exception
    {
        call("logError", message);
    }

    public void logWarning(String message) throws Exception
    {
        call("logWarning", message);
    }

    public long getAgentId(String agent) throws Exception
    {
        return Long.parseLong(this.<String>call("getAgentId", agent));
    }
    
    public void enqueueSynchronisationMessage(String agent, boolean synchronous, String description, boolean succeed) throws Exception
    {
        call("enqueueSynchronisationMessage", agent, synchronous, description, succeed);
    }

    public void installPlugin(String path) throws Exception
    {
        call("installPlugin", path);
    }

    public Vector<Hashtable<String, Object>> getRunningPlugins() throws Exception
    {
        return call("getRunningPlugins");
    }

    public void cancelActiveBuilds() throws Exception
    {
        call("cancelActiveBuilds");
    }
    
    public void ensureQueuesRunning() throws Exception
    {
        call("ensureQueuesRunning");
    }
}
