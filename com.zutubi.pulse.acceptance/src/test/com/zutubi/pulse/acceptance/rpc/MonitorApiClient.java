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
