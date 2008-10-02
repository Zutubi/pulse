package com.zutubi.pulse.master.restore;

import com.zutubi.pulse.core.test.PulseTestCase;
import com.zutubi.pulse.core.util.FileSystemUtils;
import com.zutubi.pulse.core.util.JDBCUtils;
import com.zutubi.pulse.master.hibernate.MutableConfiguration;
import com.zutubi.util.io.IOUtils;
import org.apache.commons.dbcp.BasicDataSource;
import org.hibernate.cfg.Environment;
import org.hibernate.dialect.Dialect;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Properties;

/**
 *
 *
 */
public class DatabaseArchiveTest extends PulseTestCase
{
    private File tmpDir;

    private BasicDataSource dataSource;

    protected void setUp() throws Exception
    {
        super.setUp();

        tmpDir = FileSystemUtils.createTempDir();

        dataSource = createDataSource();

    }

    protected void tearDown() throws Exception
    {
        if (dataSource != null)
        {
            dataSource.close();
        }
        
        removeDirectory(tmpDir);

        super.tearDown();
    }

    private BasicDataSource createDataSource()
    {
        BasicDataSource dataSource = new BasicDataSource();

        dataSource.setDriverClassName("org.hsqldb.jdbcDriver");
        dataSource.setUrl("jdbc:hsqldb:mem:testdb");
        dataSource.setUsername("sa");
        dataSource.setPassword("");

        return dataSource;
    }

    private String[] mappings = new String[]{"com/zutubi/pulse/master/restore/schema/Schema.hbm.xml"};

    private Properties getHibernateProperties()
    {
        Properties properties = new Properties();
        properties.setProperty("hibernate.dialect", "org.hibernate.dialect.HSQLDialect");
//        properties.put("hibernate.show_sql", "true");

        // if we want to work through hibernates API, then we need to tell it where to get the
        // datasource from.
        properties.put(Environment.CONNECTION_PROVIDER, "com.zutubi.pulse.master.hibernate.HackyConnectionProvider");

//        HackyConnectionProvider.dataSource = dataSource;
        
        return properties;
    }

    private void createSchema(MutableConfiguration configuration) throws SQLException
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

    private void createSchemaConstraints(MutableConfiguration configuration) throws SQLException
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

    public void testBackupRestore() throws ArchiveException, SQLException, IOException
    {
        MutableConfiguration configuration = new MutableConfiguration();

        // CONFIGURE HIBERNATE.
        configuration.addClassPathMappings(Arrays.asList(mappings));
        configuration.setProperties(getHibernateProperties());

        // SETUP THE DATABASE SCHEMA FOR TESTING.
        createSchema(configuration);
        createSchemaConstraints(configuration);

        // SETUP TEST DATA.
        JDBCUtils.execute(dataSource, "insert into TYPES (ID, STRING_TYPE, BOOLEAN_TYPE) values (1, 'string', '1')");
        JDBCUtils.execute(dataSource, "insert into TYPES (ID, STRING_TYPE, BOOLEAN_TYPE) values (2, '', '1')");
        JDBCUtils.execute(dataSource, "insert into TYPES (ID, STRING_TYPE, BOOLEAN_TYPE) values (3, null, '1')");
        JDBCUtils.execute(dataSource, "insert into RELATED_TYPES (ID, TYPE) values (1, 1)");

        DatabaseArchive archive = new DatabaseArchive();
        archive.setDataSource(dataSource);
        archive.setMappings(Arrays.asList(mappings));
/*
        final List<Integer> feedbackPercentages = new LinkedList<Integer>();
        archive.setFeedback(new TaskFeedback()
        {
            public void setPercetageComplete(int percentage)
            {
                feedbackPercentages.add(percentage);
            }
        });
*/
        archive.setHibernateProperties(getHibernateProperties());
        archive.backup(tmpDir);

        assertTrue(new File(tmpDir, "export.xml").exists());
        assertTrue(new File(tmpDir, "Schema.hbm.xml").exists());
        assertTrue(new File(tmpDir, "tables.properties").exists());

        Properties props = IOUtils.read(new File(tmpDir, "tables.properties"));
        assertEquals(3, props.size());
        assertEquals("3", props.get("TYPES"));
        assertEquals("1", props.get("RELATED_TYPES"));
        // third one is hibernate_unique_key.

        JDBCUtils.execute(dataSource, "SHUTDOWN");

        dataSource = createDataSource();
        
        archive.setDataSource(dataSource);
        archive.restore(tmpDir);

/*
        assertEquals(25, (int)feedbackPercentages.get(0));
        assertEquals(50, (int)feedbackPercentages.get(1));
        assertEquals(75, (int)feedbackPercentages.get(2));
        assertEquals(99, (int)feedbackPercentages.get(3));
*/

        assertEquals(3, JDBCUtils.executeCount(dataSource, "select * from TYPES"));
        assertEquals(1, JDBCUtils.executeCount(dataSource, "select * from RELATED_TYPES"));
    }

}
