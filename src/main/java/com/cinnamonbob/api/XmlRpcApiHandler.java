package com.cinnamonbob.api;

import com.cinnamonbob.BuildRequest;

/**
 * A simple java object that defines the XmlRpc interface API.
 *
 */
public class XmlRpcApiHandler
{
    /**
     * Generate a request to enqueue a build request for the specified project.
     *
     * @param projectName
     * @return true if the request was enqueued, false otherwise.
     */
    public boolean build(String projectName)
    {
        // check if the projectName is valid.
        BuildRequest request = new BuildRequest(projectName);
        System.out.println("XmlRpcApiHandler.build("+projectName+")");
        return true;
    }

}
