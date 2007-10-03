package com.zutubi.pulse.core.scm;

/**
 *
 *
 */
public interface ScmClientFactory<T extends com.zutubi.pulse.core.scm.config.ScmConfiguration>
{
    ScmClient createClient(T config) throws ScmException;
}
