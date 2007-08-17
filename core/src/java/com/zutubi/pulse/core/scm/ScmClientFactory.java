package com.zutubi.pulse.core.scm;

import com.zutubi.pulse.core.config.Configuration;

/**
 *
 *
 */
public interface ScmClientFactory<T extends Configuration>
{
    ScmClient createClient(T config) throws ScmException;
}
