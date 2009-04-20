package com.zutubi.pulse.core.scm.api;

/**
 * Creator of ScmClient instances based on configuration objects.  Each SCM
 * plugin should provide a factory which takes configurations and can make
 * matching clients.
 */
public interface ScmClientFactory<T>
{
    /**
     * Create a new ScmClient instance using the provided configuration
     * details.
     *
     * @param config    the scm client configuration
     * @return  a new scm client instance
     * @throws ScmException on error
     */
    ScmClient createClient(T config) throws ScmException;
}
