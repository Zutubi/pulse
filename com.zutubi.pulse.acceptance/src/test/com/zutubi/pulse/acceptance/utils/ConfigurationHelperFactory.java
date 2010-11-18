package com.zutubi.pulse.acceptance.utils;

import com.zutubi.pulse.acceptance.rpc.RemoteApiClient;

/**
 * Factory interface for creating ConfigurationHelper instances.
 */
public interface ConfigurationHelperFactory
{
    /**
     * Create a configuration helper instance that uses with provided
     * xmlRpcHelper to manage its communication with the remote pulse
     * server.
     *
     * @param helper    the xmlrpchelper that will be used to communicate with
     * the remote pulse server.
     *
     * @return a configuration helper instance.
     *
     * @throws Exception    on error.
     */
    ConfigurationHelper create(RemoteApiClient helper) throws Exception;
}
