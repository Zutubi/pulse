package com.zutubi.pulse.bootstrap;

import com.zutubi.pulse.upgrade.tasks.MutableConfiguration;
import com.zutubi.pulse.upgrade.tasks.HackyUpgradeTaskConnectionProvider;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import javax.sql.DataSource;
import java.util.List;
import java.util.Properties;

/**
 *
 *
 */
public class DatabaseConsoleBeanFactory implements FactoryBean
{
    private DatabaseConsole instance;

    private DatabaseConfig databaseConfig;

    private DataSource dataSource;
    
    private List<String> mappings;

    public Object getObject() throws Exception
    {
        if (instance == null)
        {
            MutableConfiguration config = new MutableConfiguration();

            Properties props = new Properties();
            props.putAll(databaseConfig.getProperties());
            props.put("hibernate.connection.provider_class", "com.zutubi.pulse.upgrade.tasks.HackyUpgradeTaskConnectionProvider");

            // a) retrieve hibernate mappings for schema generation.
            for (String mapping : mappings)
            {
                Resource r = new ClassPathResource(mapping);
                config.addInputStream(r.getInputStream());
            }

            // slight hack to provide hibernate with access to the configured datasource.
            HackyUpgradeTaskConnectionProvider.dataSource = dataSource;

            if (databaseConfig.isEmbedded())
            {
                EmbeddedHSQLDBConsole console = new EmbeddedHSQLDBConsole(databaseConfig);
                console.setDataSource(dataSource);
                console.setHibernateConfig(config);
                console.setHibernateProperties(props);
                instance = console;
            }
            else
            {
                RemoteDatabaseConsole console = new RemoteDatabaseConsole(databaseConfig);
                console.setDataSource(dataSource);
                console.setHibernateConfig(config);
                console.setHibernateProperties(props);
                instance = console;
            }
        }
        return instance;
    }

    public Class getObjectType()
    {
        return DatabaseConsole.class;
    }

    public boolean isSingleton()
    {
        return true;
    }

    public void setDataSource(DataSource dataSource)
    {
        this.dataSource = dataSource;
    }

    public void setDatabaseConfig(DatabaseConfig databaseConfig)
    {
        this.databaseConfig = databaseConfig;
    }

    public void setHibernateMappings(List<String> mappings)
    {
        this.mappings = mappings;
    }

}
