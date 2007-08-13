package com.zutubi.pulse.core.scm;

/**
 *
 *
 */
public interface ScmClientFactory<T>
{
    ScmClient createClient(T config) throws ScmException;
}
