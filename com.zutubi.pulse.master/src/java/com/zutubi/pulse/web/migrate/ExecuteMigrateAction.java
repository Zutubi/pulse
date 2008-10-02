package com.zutubi.pulse.web.migrate;

import com.zutubi.pulse.monitor.Monitor;

/**
 *
 *
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
            return "success";
        }
        if (monitor.isStarted())
        {
            return "wait";
        }

        migrationManager.runMigration();

        return "success";
    }
}
