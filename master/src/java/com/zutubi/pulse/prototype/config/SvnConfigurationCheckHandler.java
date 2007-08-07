package com.zutubi.pulse.prototype.config;

import com.zutubi.config.annotations.SymbolicName;
import com.zutubi.prototype.ConfigurationCheckHandlerSupport;
import com.zutubi.pulse.scm.ScmException;
import com.zutubi.pulse.scm.svn.config.SvnConfiguration;

/**
 *
 *
 */
@SymbolicName("zutubi.svnConfigurationCheckHandler")
public class SvnConfigurationCheckHandler extends ConfigurationCheckHandlerSupport<SvnConfiguration>
{
    public void test(SvnConfiguration configuration) throws ScmException
    {
        configuration.createClient().testConnection();
    }
}
