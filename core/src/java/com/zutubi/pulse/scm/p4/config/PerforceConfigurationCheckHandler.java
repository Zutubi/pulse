package com.zutubi.pulse.scm.p4.config;

import com.zutubi.config.annotations.SymbolicName;
import com.zutubi.pulse.core.config.ConfigurationCheckHandlerSupport;
import com.zutubi.pulse.scm.ScmException;
import com.zutubi.pulse.scm.p4.config.PerforceConfiguration;

/**
 */
@SymbolicName("zutubi.perforceConfigurationCheckHandler")
public class PerforceConfigurationCheckHandler extends ConfigurationCheckHandlerSupport<PerforceConfiguration>
{
    public void test(PerforceConfiguration configuration) throws ScmException
    {
        configuration.createClient().testConnection();
    }
}
