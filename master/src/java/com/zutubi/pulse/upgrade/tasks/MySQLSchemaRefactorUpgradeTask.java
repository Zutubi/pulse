package com.zutubi.pulse.upgrade.tasks;

import com.zutubi.pulse.util.JDBCUtils;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;

/**
 *
 *
 */
public class MySQLSchemaRefactorUpgradeTask extends AbstractSchemaRefactorUpgradeTask
{
    public String getName()
    {
        return "Schema Refactor " + getBuildNumber();
    }

    public String getDescription()
    {
        return "Refactor the schema in preparation for supporting the MySQL database.";
    }

    public boolean haltOnFailure()
    {
        return true;
    }

    protected void doRefactor(Connection con, SchemaRefactor refactor) throws SQLException
    {
        refactor.renameTable("CHANGE", "FILE_CHANGE");
        refactor.renameTable("TRIGGER", "LOCAL_TRIGGER");
        refactor.renameColumn("ARTIFACT", "INDEX", "INDEX_FILE");
        refactor.renameColumn("SUBSCRIPTION", "CONDITION", "NOTIFY_CONDITION");

        transferUserProperties(con);
    }

    private void transferUserProperties(Connection con) throws SQLException
    {
        CallableStatement stmt = null;
        try
        {
            stmt = con.prepareCall("INSERT INTO user_properties (property_key, property_value, user_id) SELECT key, value, user_id FROM user_props");
            stmt.executeUpdate();
            stmt.close();
            stmt = null;

            stmt = con.prepareCall("DROP TABLE user_props CASCADE");
            stmt.executeUpdate();
        }
        finally
        {
            JDBCUtils.close(stmt);
        }
    }
}
