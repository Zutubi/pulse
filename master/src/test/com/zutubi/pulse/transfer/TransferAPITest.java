package com.zutubi.pulse.transfer;

import com.zutubi.pulse.upgrade.tasks.MutableConfiguration;
import com.zutubi.pulse.util.JDBCUtils;
import junit.framework.TestCase;
import nu.xom.ParsingException;
import org.hibernate.dialect.Dialect;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.apache.commons.dbcp.BasicDataSource;

import javax.sql.DataSource;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

/**
 *
 *
 */
public class TransferAPITest extends TestCase
{
    private DataSource dataSource;

    protected void setUp() throws Exception
    {
        super.setUp();

        // setup the database.
        dataSource = createDataSource();
    }

    private DataSource createDataSource()
    {
        BasicDataSource dataSource = new BasicDataSource();

        dataSource.setDriverClassName("org.hsqldb.jdbcDriver");
        dataSource.setUrl("jdbc:hsqldb:mem:testdb");
        dataSource.setUsername("sa");
        dataSource.setPassword("");

        return dataSource;
    }

    protected void tearDown() throws Exception
    {
        // cleanup the database.
        ((BasicDataSource)dataSource).close();

        super.tearDown();
    }

    public void testExamine() throws IOException, SQLException, ParsingException, TransferException
    {
        MutableConfiguration configuration = new MutableConfiguration();

        // CONFIGURE HIBERNATE.
        List<Resource> mappings = getHibernateMappings();
        for (Resource mapping : mappings)
        {
            configuration.addInputStream(mapping.getInputStream());
        }
        configuration.setProperties(getHibernateProperties());

        // SETUP THE DATABASE SCHEMA FOR TESTING.
        createSchema(configuration);
        createSchemaConstraints(configuration);

        // SETUP TEST DATA.
        JDBCUtils.execute(dataSource, "insert into TYPES (ID, STRING_TYPE, BOOLEAN_TYPE) values (1, 'string', '1')");
        JDBCUtils.execute(dataSource, "insert into TYPES (ID, STRING_TYPE, BOOLEAN_TYPE) values (2, '', '1')");
        JDBCUtils.execute(dataSource, "insert into TYPES (ID, STRING_TYPE, BOOLEAN_TYPE) values (3, null, '1')");
        JDBCUtils.execute(dataSource, "insert into RELATED_TYPES (ID, TYPE) values (1, 1)");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        TransferAPI transferAPI = new TransferAPI();
        transferAPI.dump(configuration, dataSource, baos);

        JDBCUtils.execute(dataSource, "SHUTDOWN");

        dataSource = createDataSource();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());

        transferAPI.restore(configuration, dataSource, bais);

        assertEquals(3, JDBCUtils.executeCount(dataSource, "select * from TYPES"));
/*
        assertEquals("", JDBCUtils.executeSimpleQuery(dataSource, "select STRING_TYPE from TYPES where ID = 2"));
        assertNull(JDBCUtils.executeSimpleQuery(dataSource, "select STRING_TYPE from TYPES where ID = 3"));
*/
        assertEquals(1, JDBCUtils.executeCount(dataSource, "select * from RELATED_TYPES"));
    }

    private void createSchema(MutableConfiguration configuration) throws SQLException
    {
        Dialect dialect = Dialect.getDialect(configuration.getProperties());
        String[] sqlCreate = configuration.generateSchemaCreationScript(dialect);
        for (String sql : sqlCreate)
        {
            if (sql.startsWith("create"))
            {
                System.out.println(sql);
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
                System.out.println(sql);
                JDBCUtils.execute(dataSource, sql);
            }
        }
    }

    private List<Resource> getHibernateMappings()
    {
        List<Resource> mappings = new LinkedList<Resource>();
        mappings.add(new ClassPathResource("com/zutubi/pulse/transfer/Schema.hbm.xml"));
        return mappings;
    }

    private Properties getHibernateProperties()
    {
        Properties properties = new Properties();
        properties.setProperty("hibernate.dialect", "org.hibernate.dialect.HSQLDialect");
        return properties;
    }

}
