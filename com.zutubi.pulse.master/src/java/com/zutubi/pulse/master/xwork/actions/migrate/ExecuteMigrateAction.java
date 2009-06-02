package com.zutubi.pulse.master.xwork.actions.migrate;

import com.zutubi.pulse.master.util.monitor.Monitor;
import com.opensymphony.webwork.interceptor.ExecuteAndWaitInterceptor;

/**
 * Start the migration process.
 */
public class ExecuteMigrateAction extends MigrateActionSupport
{
    private Monitor monitor;

    public Monitor getMonitor()
    {
        return monitor;
    }

    public String execute() throws Exception
    {
        // get the monitor.
        monitor = migrationManager.getMonitor();
        if (monitor.isFinished())
        {
            return SUCCESS;
        }
        if (monitor.isStarted())
        {
            return ExecuteAndWaitInterceptor.WAIT;
        }

        migrationManager.runMigration();

        return SUCCESS;
    }
}
