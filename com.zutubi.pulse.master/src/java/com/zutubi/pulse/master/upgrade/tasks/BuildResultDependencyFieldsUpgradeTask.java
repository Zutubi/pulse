package com.zutubi.pulse.master.upgrade.tasks;

import com.zutubi.pulse.master.hibernate.SchemaRefactor;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Adds default values to new columns of the BUILD_RESULT table for dependencies.
 */
public class BuildResultDependencyFieldsUpgradeTask extends AbstractSchemaRefactorUpgradeTask
{
    protected void doRefactor(Connection con, SchemaRefactor refactor) throws SQLException, IOException
    {
        runUpdate(con, "update BUILD_RESULT set META_BUILD_ID = 0, STATUS = 'integration'");
    }
}
