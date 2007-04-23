package com.zutubi.pulse.prototype.config;

import com.zutubi.config.annotations.SymbolicName;
import com.zutubi.prototype.ConfigurationCheckHandler;
import com.zutubi.pulse.scm.ScmException;
import com.zutubi.pulse.servercore.config.SvnConfiguration;

/**
 *
 *
 */
@SymbolicName("internal.svnConfigurationCheckHandler")
public class SvnConfigurationCheckHandler implements ConfigurationCheckHandler<SvnConfiguration>
{
    public void test(SvnConfiguration configuration) throws ScmException
    {
        configuration.createClient().testConnection();
    }
}
