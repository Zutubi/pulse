package com.zutubi.pulse.upgrade.tasks;

import java.sql.Connection;
import java.sql.SQLException;

/**
 *
 *
 */
public class PostgresSchemaRefactorUpgradeTask extends AbstractSchemaRefactorUpgradeTask
{
    public String getName()
    {
        return "Schema Refactor " + getBuildNumber();
    }

    public String getDescription()
    {
        return "Refactor the schema in preparation for supporting the postgres database.";
    }

    public boolean haltOnFailure()
    {
        return true;
    }

    protected void doRefactor(Connection con, SchemaRefactor refactor) throws SQLException
    {
        refactor.renameTable("USER", "LOCAL_USER");
        refactor.renameColumn("CLEANUP_RULE", "limit", "RULE_LIMIT");
        refactor.renameColumn("BUILD_RESULT", "USER", "LOCAL_USER");
        refactor.renameColumn("BUILD_REASON", "USER", "LOCAL_USER");
    }
}
