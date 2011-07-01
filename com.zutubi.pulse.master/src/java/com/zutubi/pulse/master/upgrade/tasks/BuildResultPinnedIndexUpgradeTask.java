package com.zutubi.pulse.master.upgrade.tasks;

import com.zutubi.pulse.master.hibernate.SchemaRefactor;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Adds an index to the PINNED column on the BUILD_RESULT table.
 */
public class BuildResultPinnedIndexUpgradeTask extends AbstractSchemaRefactorUpgradeTask
{
    protected void doRefactor(Connection con, SchemaRefactor refactor) throws SQLException, IOException
    {
        addIndex(con, "BUILD_RESULT", "idx_buildresult_pinned", "PINNED");
    }
}
