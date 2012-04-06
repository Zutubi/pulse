package com.zutubi.pulse.master.upgrade.tasks;

import com.zutubi.pulse.master.hibernate.SchemaRefactor;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Adds indices to the new BUILD_DEPENDENCY_LINK table.
 */
public class DependencyLinkIndicesUpgradeTask extends AbstractSchemaRefactorUpgradeTask
{
    protected void doRefactor(Connection con, SchemaRefactor refactor) throws SQLException, IOException
    {
        addIndex(con, "BUILD_DEPENDENCY_LINK", "idx_builddependencylink_upstreambuildid", "UPSTREAM_BUILD_ID");
        addIndex(con, "BUILD_DEPENDENCY_LINK", "idx_builddependencylink_downstreambuildid", "DOWNSTREAM_BUILD_ID");
    }
}
