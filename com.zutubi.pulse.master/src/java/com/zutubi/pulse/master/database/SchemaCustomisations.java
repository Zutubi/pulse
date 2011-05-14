package com.zutubi.pulse.master.database;

import com.zutubi.pulse.core.util.JDBCUtils;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Hand-applied schema customisations - for things that hibernate does not support.
 */
public class SchemaCustomisations
{
    public static String[] customiseSchemaCreationScript(Connection connection, String[] generatedSql) throws SQLException
    {
        String[] result = new String[generatedSql.length + 1];
        System.arraycopy(generatedSql, 0, result, 0, generatedSql.length);
        result[generatedSql.length] = JDBCUtils.sqlAddIndex(connection, "AGENT_SYNCH_MESSAGE", "AGENT_SYNCH_MESSAGE_DESCRIPTION", "DESCRIPTION", 127);
        return result;
    }
}
