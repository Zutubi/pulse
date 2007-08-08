package com.zutubi.pulse.scm.cvs.config;

import com.zutubi.config.annotations.SymbolicName;
import com.zutubi.pulse.core.config.ConfigurationCheckHandlerSupport;
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
