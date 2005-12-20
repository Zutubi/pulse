package com.cinnamonbob.util;

import com.cinnamonbob.util.logging.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.LinkedList;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.PreparedStatement;

/**
 * <class-comment/>
 */
public class JDBCUtils
{
    private static final Logger LOG = Logger.getLogger(JDBCUtils.class);

    /**
     * Execute the given schema script on the given JDBC Connection.
     * Will log unsuccessful statements and continue to execute.
     *
     * @param con the JDBC Connection to execute the script on
     * @param sql the SQL statements to execute
     * @throws java.sql.SQLException if thrown by JDBC methods
     */
    // The following method was taken from org.springframework.orm.hibernate3.LocalSessionFactoryBean
    // and adapted slightly to fit jdk 1.5
    public static void executeSchemaScript(Connection con, String[] sql) throws SQLException
    {
        if (sql != null && sql.length > 0)
        {
            boolean oldAutoCommit = con.getAutoCommit();
            con.setAutoCommit(false);
            try
            {
                Statement stmt = con.createStatement();
                try
                {
                    for (String aSql : sql)
                    {
                        LOG.info("Executing schema statement: " + aSql);
                        try
                        {
                            stmt.executeUpdate(aSql);
                        }
                        catch (SQLException ex)
                        {
                            LOG.warning("Unsuccessful schema statement: " + aSql, ex);
                        }
                    }
                }
                finally
                {
                    JDBCUtils.close(stmt);
                }
                con.commit();
            }
            finally
            {
                con.setAutoCommit(oldAutoCommit);
            }
        }
    }

    public static String[] loadScript(InputStream in) throws IOException
    {
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));

        List<String> statements = new LinkedList<String>();

        String line;
        StringBuffer statement = new StringBuffer();
        while ((line = reader.readLine()) != null)
        {
            line = line.trim();
            if (line.length() == 0)
            {
                // ignore blank lines
                continue;
            }
            if (line.startsWith("#"))
            {
                // comment, ignore it.
                continue;
            }
            statement.append(line).append(" ");
            if (line.endsWith(";"))
            {
                // end of statement
                statements.add(statement.toString());
                statement = new StringBuffer();
            }
        }
        return statements.toArray(new String[statements.size()]);
    }

    public static void close(Statement stmt)
    {
        if (stmt != null)
        {
            try
            {
                stmt.close();
            }
            catch (SQLException e)
            {
                //noop
            }
        }
    }

    public static void close(Connection con)
    {
        if (con != null)
        {
            try
            {
                con.close();
            }
            catch (SQLException e)
            {
                // noop
            }
        }
    }

    public static void close(PreparedStatement ps)
    {
        if (ps != null)
        {
            try
            {
                ps.close();
            }
            catch (SQLException e)
            {
                // noop
            }
        }
    }
}
