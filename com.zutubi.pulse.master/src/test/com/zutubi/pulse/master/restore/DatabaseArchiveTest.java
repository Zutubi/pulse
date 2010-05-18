package com.zutubi.pulse.master.restore;

import com.zutubi.pulse.core.test.api.PulseTestCase;
import com.zutubi.pulse.core.util.JDBCUtils;
import com.zutubi.pulse.master.hibernate.MutableConfiguration;
import com.zutubi.util.io.IOUtils;
import org.apache.commons.dbcp.BasicDataSource;
import org.hibernate.dialect.Dialect;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Properties;

public class DatabaseArchiveTest extends PulseTestCase
{
    private File tmpDir;

    private BasicDataSource dataSource;

    protected void setUp() throws Exception
    {
        super.setUp();

        tmpDir = createTempDirectory();

        dataSource = createDataSource();
    }

    protected void tearDown() throws Exception
    {
        closeDatasource(dataSource);
        dataSource = null;

        removeDirectory(tmpDir);
        tmpDir = null;
        
        super.tearDown();
    }

    private BasicDataSource createDataSource()
    {
        BasicDataSource dataSource = new BasicDataSource();

        dataSource.setDriverClassName("org.hsqldb.jdbcDriver");
        dataSource.setUrl("jdbc:hsqldb:mem:test" + System.currentTimeMillis());
        dataSource.setUsername("sa");
        dataSource.setPassword("");

        return dataSource;
    }

    private String[] mappings = new String[]{
            "com/zutubi/pulse/master/restore/schema/Types.hbm.xml",
            "com/zutubi/pulse/master/restore/schema/RelatedTypes.hbm.xml"
    };

    private Properties getHibernateProperties()
    {
        Properties properties = new Properties();
        properties.setProperty("hibernate.dialect", "org.hibernate.dialect.HSQLDialect");
        properties.put("hibernate.show_sql", "true");

        return properties;
    }

    private void createSchema(MutableConfiguration configuration) throws SQLException
    {
        Dialect dialect = Dialect.getDialect(configuration.getProperties());
        String[] sqlCreate = configuration.generateSchemaCreationScript(dialect);
        for (String sql : sqlCreate)
        {
            JDBCUtils.execute(dataSource, sql);
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

        // SETUP TEST DATA.
        JDBCUtils.execute(dataSource, "insert into TYPES (ID, VALUE) values (1, 'string')");
        JDBCUtils.execute(dataSource, "insert into TYPES (ID, VALUE) values (2, 'value')");
        JDBCUtils.execute(dataSource, "insert into TYPES (ID, VALUE) values (3, 'here')");
        JDBCUtils.execute(dataSource, "insert into RELATED_TYPES (ID, TYPE) values (1, 1)");

        DatabaseArchive backupArchive = new DatabaseArchive();
        backupArchive.setMappings(Arrays.asList(mappings));
        backupArchive.setDataSource(dataSource);
        backupArchive.setHibernateProperties(getHibernateProperties());

        backupArchive.backup(tmpDir);

        assertTrue(new File(tmpDir, DatabaseArchive.EXPORT_FILENAME).exists());
        assertTrue(new File(tmpDir, "RelatedTypes.hbm.xml").exists());
        assertTrue(new File(tmpDir, "Types.hbm.xml").exists());
        assertTrue(new File(tmpDir, DatabaseArchive.TRACKING_FILENAME).exists());

        Properties props = IOUtils.read(new File(tmpDir, DatabaseArchive.TRACKING_FILENAME));
        assertEquals(3, props.size());  // 3 tables in the export
        assertEquals("3", props.get("TYPES")); // 3 entries in the types table
        assertEquals("1", props.get("RELATED_TYPES"));  // 1 entry in the related types table
        assertEquals("1", props.get("hibernate_unique_key"));  // 1 entry in the hibernate unique key table.

        closeDatasource(dataSource);

        // create an empty datasource.
        dataSource = createDataSource();

        DatabaseArchive restoreArchive = new DatabaseArchive();
        restoreArchive.setDataSource(dataSource);
        restoreArchive.setHibernateProperties(getHibernateProperties());
        restoreArchive.restore(tmpDir);

        assertEquals(3, JDBCUtils.executeCount(dataSource, "select * from TYPES"));
        assertEquals(1, JDBCUtils.executeCount(dataSource, "select * from RELATED_TYPES"));
    }

    private void closeDatasource(BasicDataSource dataSource) throws SQLException
    {
        if (dataSource != null)
        {
            JDBCUtils.execute(dataSource, "SHUTDOWN");
            dataSource.close();
        }
    }
}
