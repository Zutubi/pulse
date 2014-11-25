package com.zutubi.pulse.master.migrate;

import com.zutubi.pulse.core.test.api.PulseTestCase;
import com.zutubi.pulse.core.util.JDBCUtils;
import com.zutubi.pulse.master.database.DatabaseConfig;
import com.zutubi.pulse.master.hibernate.HibernateUtils;
import com.zutubi.pulse.master.hibernate.MutableConfiguration;
import com.zutubi.pulse.master.transfer.TransferException;
import com.zutubi.pulse.master.util.monitor.JobRunner;
import com.zutubi.pulse.master.util.monitor.Monitor;
import com.zutubi.pulse.master.util.monitor.TaskException;
import com.zutubi.pulse.master.util.monitor.TaskFeedback;
import nu.xom.ParsingException;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

public class MigrateDatabaseTaskTest extends PulseTestCase
{
    public void testMigrate() throws IOException, SQLException, ParsingException, TransferException, TaskException
    {
        Properties sourceDatabase = getDatabaseProperties("testdb1");
        Properties targetDatabase = getDatabaseProperties("testdb2");

        List<String> mappings = getHibernateMappings();

        DataSource sourceDataSource = createDataSource(sourceDatabase);

        MutableConfiguration sourceHibernateConfiguration = HibernateUtils.createHibernateConfiguration(mappings, sourceDatabase);
        
        // SETUP THE DATABASE SCHEMA FOR TESTING.
        HibernateUtils.createSchema(sourceDataSource, sourceHibernateConfiguration);
        HibernateUtils.createSchemaConstraints(sourceDataSource, sourceHibernateConfiguration);

        // SETUP TEST DATA.
        JDBCUtils.execute(sourceDataSource, "insert into TYPES (ID, STRING_TYPE, BOOLEAN_TYPE) values (1, 'string', '1')");
        JDBCUtils.execute(sourceDataSource, "insert into TYPES (ID, STRING_TYPE, BOOLEAN_TYPE) values (2, '', '1')");
        JDBCUtils.execute(sourceDataSource, "insert into TYPES (ID, STRING_TYPE, BOOLEAN_TYPE) values (3, null, '1')");
        JDBCUtils.execute(sourceDataSource, "insert into RELATED_TYPES (ID, TYPE) values (1, 1)");

        MigrateDatabaseTask task = new MigrateDatabaseTask("Migrate");
        task.setHibernateConfiguration(sourceHibernateConfiguration);
        task.setSourceJdbcProperties(sourceDatabase);
        task.setTargetJdbcProperties(targetDatabase);

        JobRunner runner = new JobRunner();
        runner.run(task);

        Monitor monitor = runner.getMonitor();

        TaskFeedback feedback = monitor.getProgress(task);

        assertTrue(monitor.isFinished());
        assertTrue(monitor.isSuccessful());

        DataSource targetDataSource = createDataSource(targetDatabase);

        assertEquals(3, JDBCUtils.executeCount(targetDataSource, "select * from TYPES"));
        assertEquals(1, JDBCUtils.executeCount(targetDataSource, "select * from RELATED_TYPES"));

        JDBCUtils.execute(sourceDataSource, "SHUTDOWN");
        JDBCUtils.execute(targetDataSource, "SHUTDOWN");
    }

    /**
     * External resource - the list of hibernate mappings files that define the schema we
     * are working with.
     *
     * @return a list of strings representing the classpath to the hibernate mappings
     */
    private List<String> getHibernateMappings()
    {
        List<String> mappings = new LinkedList<String>();
        mappings.add("com/zutubi/pulse/master/migrate/schema/Schema.hbm.xml");
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
