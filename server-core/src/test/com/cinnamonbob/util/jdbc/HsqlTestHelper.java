package com.cinnamonbob.util.jdbc;

import org.apache.commons.dbcp.BasicDataSource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.SQLException;

import com.cinnamonbob.util.logging.Logger;

/**
 * <class-comment/>
 */
public class HsqlTestHelper
{
    private static final Logger LOG = Logger.getLogger(HsqlTestHelper.class);

    /**
     * Create a simple IN MEMORY hsql database instance.
     *
     * @return dataSource referencing an in memory database.
     */
    public static DataSource createInMemoryDataSource()
    {
        BasicDataSource dataSource = new BasicDataSource();
        dataSource.setDriverClassName("org.hsqldb.jdbcDriver");
        dataSource.setUrl("jdbc:hsqldb:mem:testdb");
        dataSource.setUsername("sa");
        dataSource.setPassword("");
        return dataSource;
    }

    public static void shutdown(DataSource dataSource)
    {
        try
        {
            Connection con = dataSource.getConnection();
            Statement stmt = null;
            try
            {
                stmt = con.createStatement();
                stmt.execute("SHUTDOWN");
            }
            finally
            {
                JDBCUtils.close(stmt);
            }
        }
        catch (SQLException e)
        {
            LOG.severe(e);
        }
    }
}
