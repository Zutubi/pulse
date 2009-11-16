package com.zutubi.pulse.master.upgrade.tasks;

import com.zutubi.pulse.master.hibernate.SchemaRefactor;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * This upgrade task renames the LOCAL_TRIGGER.INTERVAL column to LOCAL_TRIGGER.TRIGGER_INTERVAL
 * since INTERVAL is the name of a function in mysql.
 */
public class RenameTriggerIntervalColumnUpgradeTask extends AbstractSchemaRefactorUpgradeTask
{
    protected void doRefactor(Connection con, SchemaRefactor refactor) throws SQLException, IOException
    {
        refactor.renameColumn("LOCAL_TRIGGER", "INTERVAL", "TRIGGER_INTERVAL");
    }
}