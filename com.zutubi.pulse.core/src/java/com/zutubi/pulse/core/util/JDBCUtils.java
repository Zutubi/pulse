/* Copyright 2017 Zutubi Pty Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.zutubi.pulse.core.util;

import com.zutubi.util.logging.Logger;

import javax.sql.DataSource;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.*;
import java.util.LinkedList;
import java.util.List;

/**
 * Utilities for talking to databases via JDBC.
 */
public class JDBCUtils
{
    private static final Logger LOG = Logger.getLogger(JDBCUtils.class);

    public enum DbType
    {
        HSQL
        {
            public MiniDialect createMiniDialect()
            {
                return new HSQLMiniDialect();
            }
        },
        MYSQL
        {
            public MiniDialect createMiniDialect()
            {
                return new MySQLMiniDialect();
            }
        },
        POSTGRESQL
        {
            public MiniDialect createMiniDialect()
            {
                return new PostgresMiniDialect();
            }
        };

        public abstract MiniDialect createMiniDialect();
    }

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
        PreparedStatement stmt = null;
        try
        {
            stmt = con.prepareStatement(sql);
            stmt.execute();
        }
        finally
        {
            JDBCUtils.close(stmt);
        }
    }

    public static Object executeSimpleQuery(DataSource ds, String sql) throws SQLException
    {
        Connection con = null;
        try
        {
            con = ds.getConnection();
            return executeSimpleQuery(con, sql);
        }
        finally
        {
            JDBCUtils.close(con);
        }
    }

    public static Object executeSimpleQuery(Connection con, String sql) throws SQLException
    {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try
        {
            stmt = con.prepareStatement(sql);
            rs = stmt.executeQuery();
            if (rs.next())
            {
                return rs.getObject(1);
            }
            else
            {
                return null;
            }
        }
        finally
        {
            JDBCUtils.close(rs);
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

    public static boolean columnExists(DataSource dataSource, String tableName, String columnName)
    {
        try
        {
            JDBCUtils.execute(dataSource, "SELECT COUNT(" + columnName + ") FROM " + tableName);
            return true;
        }
        catch (SQLException e)
        {
            return false;
        }
    }

    public static boolean columnExists(Connection con, String tableName, String columnName)
    {
        try
        {
            JDBCUtils.execute(con, "SELECT COUNT(" + columnName + ") FROM " + tableName);
            return true;
        }
        catch (SQLException e)
        {
            return false;
        }
    }

    public static String sqlAddIndex(Connection con, String tableName, String indexName, String[] columns) throws SQLException
    {
        return getMiniDialect(con).sqlAddIndex(tableName, indexName, columns);
    }

    public static String sqlAddIndex(Connection con, String tableName, String indexName, String column, int prefixLength) throws SQLException
    {
        return getMiniDialect(con).sqlAddIndex(tableName, indexName, column, prefixLength);
    }

    public static String sqlDropIndex(Connection con, String tableName, String indexName) throws SQLException
    {
        return getMiniDialect(con).sqlDropIndex(tableName, indexName);
    }

    public static void dropAllTablesFromSchema(Connection con) throws SQLException
    {
        // 1) determine which database we are dealing with.
        MiniDialect dialect = getMiniDialect(con);

        // 2) get a list of all the tables
        String[] tableNames = getSchemaTableNames(con);

        // 3) generate the list of sql statements
        List<String> statements = new LinkedList<String>();
        if (tableNames != null && tableNames.length > 0)
        {
            for (String tableName : tableNames)
            {
                statements.add(dialect.sqlDropTable(tableName));
            }
        }

        // 4) execute the sql.
        String[] sql = statements.toArray(new String[statements.size()]);
        try
        {
            dialect.preDrop(con);
            JDBCUtils.executeSchemaScript(con, sql);
        }
        finally
        {
            dialect.postDrop(con);
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

    public static String[] getSchemaColumnNames(Connection con, String table)
    {
        try
        {
            List<String> columnNames = new LinkedList<String>();

            DatabaseMetaData meta = con.getMetaData();

            ResultSet rs = null;
            try
            {
                rs = meta.getColumns(null, null, table, "%");

                while (rs.next())
                {
                    String columnName = rs.getString("COLUMN_NAME");
                    columnNames.add(columnName);
                }
                return columnNames.toArray(new String[columnNames.size()]);

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
                LOG.warning(e);
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
                LOG.warning(e);
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
                LOG.warning(e);
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
                LOG.warning(e);
            }
        }
    }

    public static void setString(PreparedStatement ps, int col, String str) throws SQLException
    {
        if (str == null)
        {
            ps.setNull(col, Types.VARCHAR);
        }
        else
        {
            ps.setString(col, str);
        }
    }

    public static void setParam(PreparedStatement ps, int col, Object obj, int type) throws SQLException
    {
        if (type == Types.BIGINT)
        {
            setLong(ps, col, (Long) obj);
        }
        else if (type == Types.INTEGER)
        {
            setInt(ps, col, (Integer) obj);
        }
        else if (type == Types.VARCHAR)
        {
            setString(ps, col, (String) obj);
        }
        else if (type == Types.BIT)
        {
            setBool(ps, col, (Boolean) obj);
        }
        else
        {
            throw new IllegalArgumentException("Unsupported type: " + type);
        }
    }

    public static String getString(ResultSet rs, String col) throws SQLException
    {
        Object result = rs.getString(col);
        if (rs.wasNull())
        {
            return null;
        }
        return (String) result;
    }

    public static void setInt(PreparedStatement ps, int col, Integer i) throws SQLException
    {
        if (i == null)
        {
            ps.setNull(col, Types.INTEGER);
        }
        else
        {
            ps.setInt(col, i);
        }
    }

    public static Integer getInt(ResultSet rs, String col) throws SQLException
    {
        Object result = rs.getInt(col);
        if (rs.wasNull())
        {
            return null;
        }
        return (Integer) result;
    }

    public static void setLong(PreparedStatement ps, int col, Long l) throws SQLException
    {
        if (l == null)
        {
            ps.setNull(col, Types.BIGINT);
        }
        else
        {
            ps.setLong(col, l);
        }
    }

    public static Long getLong(ResultSet rs, String col) throws SQLException
    {
        Object result = rs.getLong(col);
        if (rs.wasNull())
        {
            return null;
        }
        return (Long) result;
    }

    public static void setBool(PreparedStatement ps, int col, Boolean b) throws SQLException
    {
        if (b == null)
        {
            ps.setNull(col, Types.BOOLEAN);
        }
        else
        {
            ps.setBoolean(col, b);
        }
    }

    public static Boolean getBool(ResultSet rs, String col) throws SQLException
    {
        Object result = rs.getBoolean(col);
        if (rs.wasNull())
        {
            return null;
        }
        return (Boolean) result;
    }

    public static void executeUpdate(Connection con, String sql) throws SQLException
    {
        PreparedStatement ps = null;
        try
        {
            ps = con.prepareStatement(sql);
            ps.executeUpdate();
        }
        finally
        {
            JDBCUtils.close(ps);
        }
    }

    public static void executeUpdate(Connection con, String sql, Object[] args, int[] types) throws SQLException
    {
        PreparedStatement ps = null;
        try
        {
            ps = con.prepareStatement(sql);
            for (int i = 0; i < args.length; i++)
            {
                JDBCUtils.setParam(ps, i + 1, args[i], types[i]);
            }
            ps.executeUpdate();
        }
        finally
        {
            JDBCUtils.close(ps);
        }
    }

    public static int executeCount(Connection con, String sql) throws SQLException
    {
        return executeCount(con, sql, new Object[]{}, new int[]{});
    }

    public static int executeCount(Connection con, String sql, Object[] args, int[] types) throws SQLException
    {
        PreparedStatement ps = null;
        ResultSet rs = null;
        try
        {
            ps = con.prepareStatement(sql);
            for (int i = 0; i < args.length; i++)
            {
                JDBCUtils.setParam(ps, i + 1, args[i], types[i]);
            }
            rs = ps.executeQuery();
            int count = 0;
            while (rs.next())
            {
                count++;
            }
            return count;
        }
        finally
        {
            close(rs);
            close(ps);
        }
    }

    public static int executeCount(DataSource dataSource, String sql) throws SQLException
    {
        Connection con = null;
        try
        {
            con = dataSource.getConnection();
            return executeCount(con, sql);
        }
        finally
        {
            close(con);
        }
    }

    /**
     * Get the row count for the specified table.
     * @param dataSource the datasource providing a connection to the table in question
     * @param tableName the name of the table being queried.
     * @return the number of rows currently in the named table.
     * @throws SQLException on error, for instance if the named table does not exist.
     */
    public static long executeTableRowCount(DataSource dataSource, String tableName) throws SQLException
    {
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try
        {
            con = dataSource.getConnection();
            ps = con.prepareStatement("SELECT count(*) FROM " + tableName);
            rs = ps.executeQuery();
            if (rs.next())
            {
                return rs.getLong(1);
            }
            return 0;
        }
        finally
        {
            JDBCUtils.close(rs);
            JDBCUtils.close(ps);
            JDBCUtils.close(con);
        }
    }

    public static DbType getDBType(Connection con) throws SQLException
    {
        String driverName = con.getMetaData().getURL().toLowerCase();
        if (driverName.contains("hsql"))
        {
            return DbType.HSQL;
        }
        else if (driverName.contains("ostgre"))
        {
            return DbType.POSTGRESQL;
        }
        else
        {
            return DbType.MYSQL;
        }
    }

    private static MiniDialect getMiniDialect(Connection con) throws SQLException
    {
        return getDBType(con).createMiniDialect();
    }

    /**
     * The mini dialect is used to define the differences between the databases
     * SQL syntax.
     */
    private static interface MiniDialect
    {
        /**
         * This method is called prior to the sql drop table statements being executed.
         * @param con connection on which the tables will be dropped.
         * @throws SQLException on error.
         */
        void preDrop(Connection con) throws SQLException;

        /**
         * Generate a drop table sql statement for the specified tablename
         *
         * @param tableName of the table being dropped.
         * @return the sql statement that will drop the named table.
         */
        String sqlDropTable(String tableName);

        /**
         * This method is called after all the sql drop table statements have been executed.
         * @param con connetion on which the tables are dropped.
         * @throws SQLException on error
         */
        void postDrop(Connection con) throws SQLException;
        
        String sqlAddIndex(String tableName, String indexName, String... columns);

        String sqlAddIndex(String tableName, String indexName, String column, int prefixLength);
        
        String sqlDropIndex(String tableName, String indexName);
    }

    private static abstract class MiniDialectSupport implements MiniDialect
    {
        public void preDrop(Connection con) throws SQLException
        {
        }

        public void postDrop(Connection con) throws SQLException
        {
        }

        public String sqlAddIndex(String tableName, String indexName, String... columns)
        {
            String sql = "CREATE INDEX " + indexName + " ON " + tableName + " (";
            boolean first = true;
            for (String column : columns)
            {
                if (first)
                {
                    first = false;
                }
                else
                {
                    sql += ",";
                }

                sql += column;
            }

            sql += ")";
            return sql;
        }

        public String sqlAddIndex(String tableName, String indexName, String column, int prefixLength)
        {
            return sqlAddIndex(tableName, indexName, column);
        }
    }
    private static class HSQLMiniDialect extends MiniDialectSupport
    {
        public String sqlDropTable(String tableName)
        {
            return "DROP TABLE " + tableName + " IF EXISTS CASCADE";
        }

        public String sqlDropIndex(String tableName, String indexName)
        {
            return "DROP INDEX " + indexName + " IF EXISTS";            
        }
    }

    private static class MySQLMiniDialect extends MiniDialectSupport
    {
        public void preDrop(Connection con) throws SQLException
        {
            JDBCUtils.execute(con, "SET FOREIGN_KEY_CHECKS=0");
        }

        public String sqlDropTable(String tableName)
        {
            return "DROP TABLE IF EXISTS " + tableName + " CASCADE";
        }

        public void postDrop(Connection con) throws SQLException
        {
            JDBCUtils.execute(con, "SET FOREIGN_KEY_CHECKS=1");
        }

        public String sqlAddIndex(String tableName, String indexName, String column, int prefixLength)
        {
            return "CREATE INDEX " + indexName + " ON " + tableName + " (" + column + "(" + prefixLength + "))";
        }

        public String sqlDropIndex(String tableName, String indexName)
        {
            return "DROP INDEX " + indexName + " ON " + tableName;
        }
    }

    private static class PostgresMiniDialect extends MiniDialectSupport
    {
        public String sqlDropTable(String tableName)
        {
            return "DROP TABLE " + tableName + " CASCADE";
        }

        public String sqlDropIndex(String tableName, String indexName)
        {
            return "DROP INDEX " + indexName;
        }
    }
}
