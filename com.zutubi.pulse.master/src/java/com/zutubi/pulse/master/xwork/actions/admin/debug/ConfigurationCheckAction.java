package com.zutubi.pulse.master.xwork.actions.admin.debug;

import com.zutubi.pulse.master.xwork.actions.ActionSupport;
import com.zutubi.tove.config.health.ConfigurationHealthChecker;
import com.zutubi.tove.config.health.ConfigurationHealthReport;
import com.zutubi.tove.security.AccessManager;
import com.zutubi.util.logging.Logger;

/**
 * A simple admin command that triggers the configuration health checker.
 */
public class ConfigurationCheckAction extends ActionSupport
{
    private static final Logger LOG = Logger.getLogger(ConfigurationCheckAction.class);

    private ConfigurationHealthChecker configurationHealthChecker;

    @Override
    public String execute() throws Exception
    {
        accessManager.ensurePermission(AccessManager.ACTION_ADMINISTER, null);
        ConfigurationHealthReport report = configurationHealthChecker.checkAll();
        if (report.isHealthy())
        {
            LOG.info("Configuration check: All clear.");
        }
        else
        {
            LOG.warning("Configuration check: Errors detected.");
            LOG.warning(report.toString());
        }

        return SUCCESS;
    }

    public String healAll() throws Exception
    {
        accessManager.ensurePermission(AccessManager.ACTION_ADMINISTER, null);
        ConfigurationHealthReport report = configurationHealthChecker.healAll();
        if (report.isHealthy())
        {
            LOG.info("Configuration heal: All clear.");
        }
        else
        {
            LOG.warning("Configuration heal: Errors detected.");
            LOG.warning(report.toString());
        }

        return SUCCESS;
    }

    public void setConfigurationHealthChecker(ConfigurationHealthChecker configurationHealthChecker)
    {
        this.configurationHealthChecker = configurationHealthChecker;
    }
}
