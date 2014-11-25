package com.zutubi.pulse.master.transfer;

import com.zutubi.pulse.core.util.JDBCUtils;
import com.zutubi.pulse.master.database.DatabaseConfig;
import com.zutubi.pulse.master.hibernate.HibernateUtils;
import com.zutubi.pulse.master.hibernate.MutableConfiguration;
import com.zutubi.util.junit.ZutubiTestCase;
import nu.xom.ParsingException;

import javax.sql.DataSource;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

public class TransferAPITest extends ZutubiTestCase
{
    public void testSimpleDumpAndRestore() throws IOException, SQLException, ParsingException, TransferException
    {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        TransferAPI transferAPI = new TransferAPI();

        Properties sourceDatabase = getDatabaseProperties("testdb");

        List<String> mappings = getHibernateMappings();
        MutableConfiguration configuration = HibernateUtils.createHibernateConfiguration(mappings, sourceDatabase);

        DataSource dataSource = null;
        try
        {
            dataSource = createDataSource(sourceDatabase);
            createTestData(dataSource, configuration);

            transferAPI.dump(configuration, dataSource, baos);
        }
        finally
        {
            JDBCUtils.execute(dataSource, "SHUTDOWN");
        }

        try
        {
            dataSource = createDataSource(sourceDatabase);

            ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());

            transferAPI.restore(configuration, dataSource, bais);

            assertEquals(3, JDBCUtils.executeCount(dataSource, "select * from TYPES"));
            assertEquals(1, JDBCUtils.executeCount(dataSource, "select * from RELATED_TYPES"));
        }
        finally
        {
            JDBCUtils.execute(dataSource, "SHUTDOWN");
        }
    }

    public void testSchemaConsistencyCheckOnRestore() throws IOException, SQLException, TransferException
    {
        Properties sourceDatabase = getDatabaseProperties("testdb");
        List<String> mappings = getHibernateMappings();
        MutableConfiguration configuration = HibernateUtils.createHibernateConfiguration(mappings, sourceDatabase);

        DataSource dataSource = null;
        try
        {
            dataSource = createDataSource(sourceDatabase);
            createTestData(dataSource, configuration);

            // setup the incompatible configuration.
            List<String> incompatibleMappings = new LinkedList<String>();
            incompatibleMappings.add("com/zutubi/pulse/master/transfer/Schema_Animals.hbm.xml");
            MutableConfiguration incompatibleConfiguration = HibernateUtils.createHibernateConfiguration(incompatibleMappings, sourceDatabase);

            TransferAPI transferAPI = new TransferAPI();
            try
            {
                transferAPI.dump(incompatibleConfiguration, dataSource, new ByteArrayOutputStream());
                fail();
            }
            catch (TransferException e)
            {
                // expected
                assertEquals("Schema export aborted due to schema / mapping mismatch", e.getMessage());
            }
        }
        finally
        {
            JDBCUtils.execute(dataSource, "SHUTDOWN");
        }
    }

    public void testMigrateDatabase() throws SQLException, IOException, TransferException
    {
        Properties sourceDatabase = getDatabaseProperties("testdb");
        Properties targetDatabase = getDatabaseProperties("target");

        List<String> mappings = getHibernateMappings();
        MutableConfiguration configuration = HibernateUtils.createHibernateConfiguration(mappings, sourceDatabase);

        DataSource dataSource = null;
        DataSource dataTarget = null;
        try
        {
            dataSource = createDataSource(sourceDatabase);
            dataTarget = createDataSource(targetDatabase);

            createTestData(dataSource, configuration);

            TransferAPI transfer = new TransferAPI();
            transfer.migrate(configuration, dataSource, configuration, dataTarget);

            assertEquals(3, JDBCUtils.executeCount(dataTarget, "select * from TYPES"));
            assertEquals(1, JDBCUtils.executeCount(dataTarget, "select * from RELATED_TYPES"));
        }
        finally
        {
            JDBCUtils.execute(dataSource, "SHUTDOWN");
            JDBCUtils.execute(dataTarget, "SHUTDOWN");
        }
    }

    private void createTestData(DataSource dataSource, MutableConfiguration configuration) throws SQLException
    {
        // SETUP THE DATABASE SCHEMA FOR TESTING.
        HibernateUtils.createSchema(dataSource, configuration);
        HibernateUtils.createSchemaConstraints(dataSource, configuration);

        // SETUP TEST DATA.
        JDBCUtils.execute(dataSource, "insert into TYPES (ID, STRING_TYPE, BOOLEAN_TYPE) values (1, 'string', '1')");
        JDBCUtils.execute(dataSource, "insert into TYPES (ID, STRING_TYPE, BOOLEAN_TYPE) values (2, '', '1')");
        JDBCUtils.execute(dataSource, "insert into TYPES (ID, STRING_TYPE, BOOLEAN_TYPE) values (3, null, '1')");
        JDBCUtils.execute(dataSource, "insert into RELATED_TYPES (ID, TYPE) values (1, 1)");
    }

    private List<String> getHibernateMappings()
    {
        List<String> mappings = new LinkedList<String>();
        mappings.add("com/zutubi/pulse/master/transfer/Schema.hbm.xml");
        return mappings;
    }

    /**
     * Get the database properties for an in memory hsqldb instance of the given name.
     *
     * @param name of the database.
     *
     * @return properties that can be used to create a jdbc data source.
     */
    private Properties getDatabaseProperties(String name)
    {
        Properties props = new Properties();
        props.put(DatabaseConfig.JDBC_DRIVER_CLASS_NAME, "org.hsqldb.jdbcDriver");
        props.put(DatabaseConfig.JDBC_URL, "jdbc:hsqldb:mem:" + name);
        props.put(DatabaseConfig.JDBC_USERNAME, "sa");
        props.put(DatabaseConfig.JDBC_PASSWORD, "");
        return props;
    }

    private DataSource createDataSource(Properties jdbcProperties)
    {
        DatabaseConfig config = new DatabaseConfig(jdbcProperties);
        return config.createDataSource();
    }
}
