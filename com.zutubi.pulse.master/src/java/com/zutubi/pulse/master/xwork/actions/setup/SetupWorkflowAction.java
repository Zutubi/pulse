package com.zutubi.pulse.master.xwork.actions.setup;

import com.zutubi.pulse.master.bootstrap.SetupManager;
import com.zutubi.pulse.master.bootstrap.SetupState;
import com.zutubi.pulse.master.migrate.MigrationManager;
import com.zutubi.pulse.master.restore.RestoreManager;
import com.zutubi.pulse.master.upgrade.UpgradeManager;
import com.zutubi.pulse.master.util.monitor.Monitor;

/**
 * Redirects to the correct action based on the current setup state.
 */
public class SetupWorkflowAction extends SetupActionSupport
{
    private static final String RESTORE_IN_PROGRESS = "restoreInProgress";
    private static final String MIGRATE_IN_PROGRESS = "migrateInProgress";
    private static final String UPGRADE_IN_PROGRESS = "upgradeInProgress";

    private UpgradeManager upgradeManager;
    private SetupManager setupManager;
    private MigrationManager migrationManager;
    private RestoreManager restoreManager;

    public String execute() throws Exception
    {
        SetupState state = setupManager.getCurrentState();
        if (state == SetupState.RESTORE)
        {
            Monitor monitor = restoreManager.getMonitor();
            if (isInProgress(monitor))
            {
                return RESTORE_IN_PROGRESS;
            }
        }
        if (state == SetupState.MIGRATE)
        {
            Monitor monitor = migrationManager.getMonitor();
            if (isInProgress(monitor))
            {
                return MIGRATE_IN_PROGRESS;
            }
        }
        if (state == SetupState.UPGRADE)
        {
            Monitor monitor = upgradeManager.getMonitor();
            if (isInProgress(monitor))
            {
                return UPGRADE_IN_PROGRESS;
            }
        }
        return state.toString().toLowerCase();
    }

    private boolean isInProgress(Monitor monitor)
    {
        return monitor != null && monitor.isStarted();
    }

    public void setSetupManager(SetupManager setupManager)
    {
        this.setupManager = setupManager;
    }

    public void setUpgradeManager(UpgradeManager upgradeManager)
    {
        this.upgradeManager = upgradeManager;
    }

    public void setMigrationManager(MigrationManager migrationManager)
    {
        this.migrationManager = migrationManager;
    }

    public void setRestoreManager(RestoreManager restoreManager)
    {
        this.restoreManager = restoreManager;
    }
}
