package com.zutubi.pulse.prototype.config;

import com.zutubi.config.annotations.SymbolicName;
import com.zutubi.prototype.ConfigurationCheckHandlerSupport;
import com.zutubi.pulse.scm.ScmException;
import com.zutubi.pulse.servercore.scm.p4.config.PerforceConfiguration;

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
