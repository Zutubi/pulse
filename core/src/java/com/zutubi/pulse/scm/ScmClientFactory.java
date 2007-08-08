package com.zutubi.pulse.scm;

/**
 *
 *
 */
public interface ScmClientFactory<T>
{
    ScmClient createClient(T config) throws ScmException;
}
