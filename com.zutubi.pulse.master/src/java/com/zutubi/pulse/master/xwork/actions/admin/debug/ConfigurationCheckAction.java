package com.zutubi.pulse.master.xwork.actions.admin.debug;

import com.zutubi.pulse.master.xwork.actions.ActionSupport;
import com.zutubi.tove.config.health.ConfigurationHealthChecker;
import com.zutubi.tove.config.health.ConfigurationHealthReport;
import com.zutubi.util.bean.ObjectFactory;
import com.zutubi.util.logging.Logger;

/**
 * A simple admin command that triggers the configuration health checker.
 */
public class ConfigurationCheckAction extends ActionSupport
{
    private static final Logger LOG = Logger.getLogger(ConfigurationCheckAction.class);

    private ObjectFactory objectFactory;

    @Override
    public String execute() throws Exception
    {
        ConfigurationHealthChecker checker = objectFactory.buildBean(ConfigurationHealthChecker.class);
        ConfigurationHealthReport report = checker.checkAll();
        if (report.isHealthy())
        {
            LOG.info("Configuration check: All clear.");
        }
        else
        {
            LOG.error("Configuration check: Errors detected.");
            LOG.warning(report.toString());
        }

        return SUCCESS;
    }

    public void setObjectFactory(ObjectFactory objectFactory)
    {
        this.objectFactory = objectFactory;
    }
}
