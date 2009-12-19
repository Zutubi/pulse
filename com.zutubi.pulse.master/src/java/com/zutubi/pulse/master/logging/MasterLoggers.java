package com.zutubi.pulse.master.logging;

import com.zutubi.util.logging.Logger;

/**
 * Provides access to master-specific loggers that are configurable using
 * system settings.
 */
public class MasterLoggers
{
    private static final String NAME_CONFIG_AUDIT = "com.zutubi.pulse.master.config.audit";

    private static Logger configAuditLogger;

    /**
     * Returns the logger used to write config audit logs when they are
     * enabled.
     *
     * @return the config audit logger
     */
    public static synchronized Logger getConfigAuditLogger()
    {
        if (configAuditLogger == null)
        {
            configAuditLogger = Logger.getLogger(NAME_CONFIG_AUDIT);
            configAuditLogger.setUseParentHandlers(false);
        }

        return configAuditLogger;
    }
}
