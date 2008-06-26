package com.zutubi.pulse.transfer;

import com.zutubi.pulse.bootstrap.ComponentContext;
import com.zutubi.pulse.transfer.jdbc.HibernateTransferSource;
import com.zutubi.pulse.transfer.jdbc.HibernateTransferTarget;
import com.zutubi.pulse.upgrade.tasks.MutableConfiguration;
import junit.framework.TestCase;
import org.apache.commons.dbcp.BasicDataSource;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Properties;

/**
 *
 *
 */
public class ExportImportSanityTest extends TestCase
{
    public void testExports() throws IOException, TransferException
    {
/*
        runExportImportJDBC();
        runExportImportXML();
*/
    }

    public void runExportImportJDBC() throws IOException, TransferException
    {
        ComponentContext.addClassPathContextDefinitions("classpath:/com/zutubi/pulse/bootstrap/context/hibernateMappingsContext.xml");

        MutableConfiguration configuration = new MutableConfiguration();

        List<String> mappings = (List<String>) ComponentContext.getBean("hibernateMappings");
        configuration.addClassPathMappings(mappings);

        Properties hibernateProperties = new Properties();
        hibernateProperties.setProperty("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
        configuration.setProperties(hibernateProperties);

        BasicDataSource source = new BasicDataSource();
        source.setDriverClassName("org.postgresql.Driver");
        source.setUrl("jdbc:postgresql://localhost:5432/pulse");
        source.setUsername("postgres");
        source.setPassword("postgres");

        BasicDataSource target = new BasicDataSource();
        target.setDriverClassName("org.postgresql.Driver");
        target.setUrl("jdbc:postgresql://localhost:5432/pulse2");
        target.setUsername("postgres");
        target.setPassword("postgres");

        HibernateTransferSource transferSource = new HibernateTransferSource();
        transferSource.setConfiguration(configuration);
        transferSource.setDataSource(source);

        HibernateTransferTarget transferTarget = new HibernateTransferTarget();
        transferTarget.setConfiguration(configuration);
        transferTarget.setDataSource(target);

        transferSource.transferTo(transferTarget);
    }

    public void runExportImportXML() throws IOException, TransferException
    {
        ComponentContext.addClassPathContextDefinitions("classpath:/com/zutubi/pulse/bootstrap/context/hibernateMappingsContext.xml");

        MutableConfiguration configuration = new MutableConfiguration();

        List<String> mappings = (List<String>) ComponentContext.getBean("hibernateMappings");
        configuration.addClassPathMappings(mappings);

        Properties hibernateProperties = new Properties();
        hibernateProperties.setProperty("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
        configuration.setProperties(hibernateProperties);

        BasicDataSource source = new BasicDataSource();
        source.setDriverClassName("org.postgresql.Driver");
        source.setUrl("jdbc:postgresql://localhost:5432/postgres");
        source.setUsername("postgres");
        source.setPassword("postgres");

        ByteArrayOutputStream out = new ByteArrayOutputStream();

        TransferAPI transferAPI = new TransferAPI();
        transferAPI.dump(configuration, source, out);

        BasicDataSource target = new BasicDataSource();
        target.setDriverClassName("org.postgresql.Driver");
        target.setUrl("jdbc:postgresql://localhost:5432/pulse");
        target.setUsername("postgres");
        target.setPassword("postgres");

        transferAPI.restore(configuration, target, new ByteArrayInputStream(out.toByteArray()));
    }
}
