package com.zutubi.pulse.prototype.config;

import com.zutubi.config.annotations.SymbolicName;
import com.zutubi.prototype.ConfigurationCheckHandlerSupport;
import com.zutubi.pulse.scm.ScmException;
import com.zutubi.pulse.scm.cvs.config.CvsConfiguration;

/**
 *
 *
 */
@SymbolicName("zutubi.cvsConfigurationCheckHandler")
public class CvsConfigurationCheckHandler extends ConfigurationCheckHandlerSupport<CvsConfiguration>
{
    public void test(CvsConfiguration configuration) throws ScmException
    {
        configuration.createClient().testConnection();
    }
}
