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
 * An XML-RPC client for {@link com.zutubi.pulse.master.api.MonitorApi}.
 */
public class MonitorApiClient extends ApiClient
{
    private static final String API_NAME = "MonitorApi";

    public MonitorApiClient(RpcClient rpc)
    {
        super(API_NAME, rpc);
    }

    public Hashtable<String, Object> getStatusForAllProjects(boolean includePersonal, String lastTimestamp) throws Exception
    {
        return call("getStatusForAllProjects", includePersonal, lastTimestamp);
    }

    public Hashtable<String, Object> getStatusForMyProjects(boolean includePersonal, String lastTimestamp) throws Exception
    {
        return call("getStatusForMyProjects", includePersonal, lastTimestamp);
    }

    public Hashtable<String, Object> getStatusForProjects(Vector<String> projects, boolean includePersonal, String lastTimestamp) throws Exception
    {
        return call("getStatusForProjects", projects, includePersonal, lastTimestamp);
    }
}
