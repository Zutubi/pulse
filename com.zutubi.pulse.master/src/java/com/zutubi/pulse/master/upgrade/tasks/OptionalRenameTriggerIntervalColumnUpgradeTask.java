package com.zutubi.pulse.master.upgrade.tasks;

import com.zutubi.pulse.master.hibernate.SchemaRefactor;
import com.zutubi.pulse.core.util.JDBCUtils;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * This upgrade task optionally renames the LOCAL_TRIGGER.INTERVAL column to LOCAL_TRIGGER.TRIGGER_INTERVAL
 * since INTERVAL is the name of a function in mysql.
 */
public class OptionalRenameTriggerIntervalColumnUpgradeTask extends AbstractSchemaRefactorUpgradeTask
{
    protected void doRefactor(Connection con, SchemaRefactor refactor) throws SQLException, IOException
    {
        if (JDBCUtils.columnExists(con, "LOCAL_TRIGGER", "INTERVAL"))
        {
            refactor.renameColumn("LOCAL_TRIGGER", "INTERVAL", "TRIGGER_INTERVAL");
        }
        else
        {
            addError(I18N.format("skipped"));
        }
    }

    @Override
    public boolean haltOnFailure()
    {
        return false;
    }
}