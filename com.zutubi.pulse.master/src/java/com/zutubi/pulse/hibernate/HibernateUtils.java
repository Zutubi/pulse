package com.zutubi.pulse.hibernate;

import com.zutubi.pulse.util.JDBCUtils;
import org.hibernate.cfg.Environment;
import org.hibernate.dialect.Dialect;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * <class-comment/>
 */
public class HibernateUtils
{
    /**
     * Emulate hibernates hilo key generation.
     *
     * @param con
     *
     * @return
     *
     * @throws SQLException
     */
    public static long getNextId(Connection con) throws SQLException
    {
        PreparedStatement ps = null;
        ResultSet rs = null;
        long nextHi;

        try
        {
            ps = con.prepareStatement("select next_hi from hibernate_unique_key");
            rs = ps.executeQuery();
            rs.next();
            nextHi = rs.getLong(1);
        }
        finally
        {
            JDBCUtils.close(ps);
            JDBCUtils.close(rs);
        }

        try
        {
            ps = con.prepareStatement("UPDATE hibernate_unique_key SET next_hi = ?");
            JDBCUtils.setLong(ps, 1, nextHi + 2);
            ps.executeUpdate();
        }
        finally
        {
            JDBCUtils.close(ps);
        }

        return getNextId(nextHi + 1);
    }

    private static long getNextId(long nextHi)
    {
        // Emulate Hibernate's hilo algorithm
        return nextHi * (Short.MAX_VALUE + 1) + 1;
    }

    public static void ensureNextId(Connection con, long lastId) throws SQLException
    {
        PreparedStatement ps = null;
        ResultSet rs = null;
        long nextHi;

        try
        {
            ps = con.prepareStatement("select next_hi from hibernate_unique_key");
            rs = ps.executeQuery();
            rs.next();
            nextHi = rs.getLong(1);
        }
        finally
        {
            JDBCUtils.close(ps);
            JDBCUtils.close(rs);
        }

        boolean updateRequired = false;
        while(lastId >= getNextId(nextHi))
        {
            updateRequired = true;
            nextHi++;
        }

        if (updateRequired)
        {
            try
            {
                ps = con.prepareStatement("UPDATE hibernate_unique_key SET next_hi = ?");
                JDBCUtils.setLong(ps, 1, nextHi);
                ps.executeUpdate();
            }
            finally
            {
                JDBCUtils.close(ps);
            }
        }
    }

    public static void createSchema(DataSource dataSource, MutableConfiguration configuration) throws SQLException
    {
        Dialect dialect = Dialect.getDialect(configuration.getProperties());
        String[] sqlCreate = configuration.generateSchemaCreationScript(dialect);
        for (String sql : sqlCreate)
        {
            if (sql.startsWith("create"))
            {
                JDBCUtils.execute(dataSource, sql);
            }
        }
    }

    public static void createSchemaConstraints(DataSource dataSource, MutableConfiguration configuration) throws SQLException
    {
        Dialect dialect = Dialect.getDialect(configuration.getProperties());
        String[] sqlAlter = configuration.generateSchemaCreationScript(dialect);
        for (String sql : sqlAlter)
        {
            if (sql.startsWith("alter"))
            {
                JDBCUtils.execute(dataSource, sql);
            }
        }
    }

    public static MutableConfiguration createHibernateConfiguration(List<String> mappings, Properties jdbcProperties) throws IOException
    {
        MutableConfiguration configuration = new MutableConfiguration();
        configuration.addClassPathMappings(mappings);

        Properties hibernateProperties = new Properties();
        hibernateProperties.put(Environment.DIALECT, inferHibernateDialect(jdbcProperties));
        configuration.setProperties(hibernateProperties);

        return configuration;
    }

    public static String inferHibernateDialect(Properties jdbcProperties)
    {
        Map<String, String> databaseTypeToDialectMapping = new HashMap<String, String>();
        databaseTypeToDialectMapping.put("hsqldb", "org.hibernate.dialect.HSQLDialect");
        databaseTypeToDialectMapping.put("mysql", "org.hibernate.dialect.MySQLDialect");
        databaseTypeToDialectMapping.put("postgresql", "org.hibernate.dialect.PostgreSQLDialect");

        String jdbcUrl = jdbcProperties.getProperty("jdbc.url");

        // format jdbc:<databaseType>:<databaseConfig>.  We are after the database type.

        if (!jdbcUrl.startsWith("jdbc:"))
        {
            throw new RuntimeException("Unable to infer hibernate dialect from jdbc url: " + jdbcUrl + ".  Expecting 'jdbc:' prefix.");
        }

        String databaseType = jdbcUrl.substring(5, jdbcUrl.indexOf(':', 5));

        if (!databaseTypeToDialectMapping.containsKey(databaseType))
        {
            throw new RuntimeException("Unsupported database type: " + databaseType + ".  No hibernate dialect available for this database type.");
        }

        return databaseTypeToDialectMapping.get(databaseType);
    }

}
