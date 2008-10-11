package com.zutubi.pulse.core.scm.api;

/**
 * Creator of ScmClient instances based on configuration objects.  Each SCM
 * plugin should provide a factory which takes configurations and can make
 * matching clients.
 */
public interface ScmClientFactory<T>
{
    ScmClient createClient(T config) throws ScmException;
}
