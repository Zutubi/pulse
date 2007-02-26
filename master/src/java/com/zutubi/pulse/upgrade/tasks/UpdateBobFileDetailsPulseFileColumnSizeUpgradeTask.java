package com.zutubi.pulse.upgrade.tasks;

import com.zutubi.pulse.upgrade.UpgradeContext;
import com.zutubi.pulse.util.JDBCUtils;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.CallableStatement;

/**
 *
 *
 */
public class UpdateBobFileDetailsPulseFileColumnSizeUpgradeTask extends AbstractSchemaRefactorUpgradeTask
{
    public String getName()
    {
        return "Schema Refactor " + getBuildNumber();
    }

    public String getDescription()
    {
        return "Upgrade the column size of the BOB_FILE_DETAILS.PULSE_FILE column to store larger custom pulse files.";
    }

    public boolean haltOnFailure()
    {
        return true;
    }

    protected void doRefactor(UpgradeContext context, Connection con, SchemaRefactor refactor) throws SQLException
    {
        refactor.refreshColumn("BOB_FILE_DETAILS", "PULSE_FILE");
    }
}
