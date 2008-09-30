package com.zutubi.pulse.core.scm;

/**
 * Creator of ScmClient instances based on configuration objects.  Each SCM
 * plugin should provide a factory which takes configurations and can make
 * matching clients.
 */
public interface ScmClientFactory<T extends com.zutubi.pulse.core.scm.config.ScmConfiguration>
{
    ScmClient createClient(T config) throws ScmException;
}
