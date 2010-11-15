package com.zutubi.pulse.master.upgrade.tasks;

import com.zutubi.pulse.master.hibernate.SchemaRefactor;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Adds an index to the project id column on the TEST_CASE_INDEX table.
 */
public class TestCaseIndexIndicesUpgradeTask extends AbstractSchemaRefactorUpgradeTask
{
    protected void doRefactor(Connection con, SchemaRefactor refactor) throws SQLException, IOException
    {
        addIndex(con, "TEST_CASE_INDEX", "idx_testcaseindex_projectid", "PROJECT_ID");
        addIndex(con, "TEST_CASE_INDEX", "idx_testcaseindex_nodeid", "NODE_ID");
    }
}
