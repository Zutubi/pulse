package com.zutubi.pulse.database;

import com.zutubi.pulse.hibernate.HackyConnectionProvider;
import com.zutubi.pulse.hibernate.MutableConfiguration;
import org.springframework.beans.factory.FactoryBean;
import org.hibernate.cfg.Environment;

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
    
    // awkard... having the console know about the hibernate mappings means we can not use it
    // as easily for tasks that do not require mappings.
    private List<String> mappings;

    public Object getObject() throws Exception
    {
        if (instance == null)
        {
            Properties props = new Properties();
            props.putAll(databaseConfig.getProperties());
            props.put(Environment.CONNECTION_PROVIDER, "com.zutubi.pulse.hibernate.HackyConnectionProvider");

            // a) retrieve hibernate mappings for schema generation.
            MutableConfiguration config = new MutableConfiguration();
            config.addClassPathMappings(mappings);

            // slight hack to provide hibernate with access to the configured datasource.
            HackyConnectionProvider.dataSource = dataSource;

            // awkard... having the console know about the hibernate mappings means we can not use it 
            // as easily for tasks that do not require mappings.

            if (databaseConfig.getUrl().contains(":hsqldb:"))
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
