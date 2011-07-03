package com.zutubi.pulse.master.upgrade.tasks;

import com.zutubi.pulse.master.hibernate.SchemaRefactor;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Drops the RECIPE_RESULT_NODE table and columns that reference it.
 */
public class DropBuildNodeTableUpgradeTask extends AbstractSchemaRefactorUpgradeTask
{
    protected void doRefactor(Connection con, SchemaRefactor refactor) throws SQLException, IOException
    {
        refactor.dropColumn("BUILD_RESULT", "RECIPE_RESULT_ID");
        refactor.dropTable("RECIPE_RESULT_NODE");
    }
}
