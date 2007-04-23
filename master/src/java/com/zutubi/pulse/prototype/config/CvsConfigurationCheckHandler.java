package com.zutubi.pulse.prototype.config;

import com.zutubi.config.annotations.SymbolicName;
import com.zutubi.prototype.ConfigurationCheckHandler;
import com.zutubi.pulse.scm.ScmException;
import com.zutubi.pulse.servercore.config.CvsConfiguration;

/**
 *
 *
 */
@SymbolicName("internal.cvsConfigurationCheckHandler")
public class CvsConfigurationCheckHandler implements ConfigurationCheckHandler<CvsConfiguration>
{
    public void test(CvsConfiguration configuration) throws ScmException
    {
        configuration.createClient().testConnection();
    }
}
