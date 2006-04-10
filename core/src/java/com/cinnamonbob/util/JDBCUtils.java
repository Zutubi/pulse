package com.zutubi.pulse.util;

import com.zutubi.pulse.util.logging.Logger;

import javax.sql.DataSource;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.*;
import java.util.LinkedList;
import java.util.List;

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

    public static void execute(DataSource ds, String sql) throws SQLException
    {
        Connection con = null;
        try
        {
            con = ds.getConnection();
            execute(con, sql);
        }
        finally
        {
            JDBCUtils.close(con);
        }
    }

    public static void execute(Connection con, String sql) throws SQLException
    {
        CallableStatement stmt = null;
        try
        {
            stmt = con.prepareCall(sql);
            stmt.execute();
        }
        finally
        {
            JDBCUtils.close(stmt);
        }
    }

    public static boolean tableExists(DataSource ds, String tableName)
    {
        try
        {
            JDBCUtils.execute(ds, "SELECT COUNT(*) FROM " + tableName);
            return true;
        }
        catch (SQLException e)
        {
            return false;
        }
    }

    public static boolean tableExists(Connection con, String tableName)
    {
        try
        {
            JDBCUtils.execute(con, "SELECT COUNT(*) FROM " + tableName);
            return true;
        }
        catch (SQLException e)
        {
            return false;
        }
    }

    public static String[] getSchemaTableNames(Connection con)
    {
        try
        {
            List<String> tableNames = new LinkedList<String>();

            DatabaseMetaData meta = con.getMetaData();

            ResultSet rs = null;
            try
            {
                rs = meta.getTables(null, null, "%", new String[]{"TABLE"});
                while (rs.next())
                {
                    String tableName = rs.getString("TABLE_NAME");
                    tableNames.add(tableName);
                }
                return tableNames.toArray(new String[tableNames.size()]);

            }
            finally
            {
                close(rs);
            }
        }
        catch (SQLException e)
        {
            LOG.severe(e);
        }
        return null;
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
                LOG.debug(e);
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
                LOG.debug(e);
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
                LOG.debug(e);
            }
        }
    }

    public static void close(ResultSet rs)
    {
        if (rs != null)
        {
            try
            {
                rs.close();
            }
            catch (SQLException e)
            {
                LOG.debug(e);
            }
        }
    }
}
