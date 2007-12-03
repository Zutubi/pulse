package com.zutubi.pulse.upgrade.tasks;

import com.zutubi.pulse.upgrade.UpgradeContext;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * A refactor that blows away the changelist uid column.
 */
public class ChangelistHashSchemaRefactorUpgradeTask extends AbstractSchemaRefactorUpgradeTask
{
    public String getName()
    {
        return "Changelist hash (drop column)";
    }

    public String getDescription()
    {
        return "Drops a column no longer required after changelists are hashed";
    }

    public boolean haltOnFailure()
    {
        return true;
    }

    protected void doRefactor(UpgradeContext context, Connection con, SchemaRefactor refactor) throws SQLException
    {
        refactor.dropColumn("BUILD_CHANGELIST", "SERVER_UID");
    }
}
