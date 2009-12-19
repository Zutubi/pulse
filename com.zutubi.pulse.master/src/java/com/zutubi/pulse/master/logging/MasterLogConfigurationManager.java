package com.zutubi.pulse.master.logging;

import com.zutubi.pulse.master.bootstrap.MasterLogConfiguration;
import com.zutubi.pulse.servercore.util.logging.LogConfigurationManager;

/**
 * Extension of the shared log configuration manager that handles master-
 * specific details.
 */
public class MasterLogConfigurationManager extends LogConfigurationManager
{
    @Override
    public void applyConfig()
    {
        super.applyConfig();
        MasterLogConfiguration logConfig = (MasterLogConfiguration) getLogConfig();
        setLoggingEnabled(MasterLoggers.getConfigAuditLogger(), logConfig.isConfigAuditLoggingEnabled());        
    }
}
