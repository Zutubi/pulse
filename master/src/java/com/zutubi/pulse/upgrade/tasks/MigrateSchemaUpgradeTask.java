package com.zutubi.pulse.upgrade.tasks;

import com.zutubi.pulse.database.DatabaseConfig;
import com.zutubi.pulse.upgrade.DataSourceAware;
import com.zutubi.pulse.upgrade.UpgradeException;
import org.springframework.core.io.ClassPathResource;

import javax.sql.DataSource;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

/**
 * <class-comment/>
 */
public class MigrateSchemaUpgradeTask implements DataSourceAware, PulseUpgradeTask
{
    private List<String> mappings = new LinkedList<String>();

    private List<String> errors = new LinkedList<String>();

    protected DataSource dataSource;

    private int buildNumber;

    private DatabaseConfig databaseConfig;

    public int getBuildNumber()
    {
        return buildNumber;
    }

    public void setBuildNumber(int buildNumber)
    {
        this.buildNumber = buildNumber;
    }

    public String getDescription()
    {
        return "The schema upgrade associated with build " + buildNumber;
    }

    public String getName()
    {
        return "Schema upgrade (" + getBuildNumber() + ")";
    }

    public List<String> getErrors()
    {
        return errors;
    }

    public boolean hasFailed()
    {
        return getErrors().size() > 0;
    }

    public boolean haltOnFailure()
    {
        return true;
    }

    public void execute() throws UpgradeException
    {
        try
        {
            // manually setup the hibernate configuration
            MutableConfiguration config = new MutableConfiguration();

            // load these properties from the context, same place that all the other
            // properties are defined.
            Properties props = databaseConfig.getHibernateProperties();
            props.put("hibernate.connection.provider_class", "com.zutubi.pulse.upgrade.tasks.HackyConnectionProvider");

            // slight hack to provide hibernate with access to the configured datasource.
            HackyConnectionProvider.dataSource = dataSource;

            // use spring to help load the classpath resources. Rather useful actually.
            for (String mapping : mappings)
            {
                ClassPathResource resource = new ClassPathResource(mapping);
                config.addInputStream(resource.getInputStream());
            }

            // run the schema update.
            SchemaRefactor refactor = new SchemaRefactor(config, props);
            refactor.sync();

            List<Exception> exceptions = refactor.getExceptions();
            for (Exception e : exceptions)
            {
                getErrors().add(e.getClass().getName() + ": Cause: " + e.getMessage());
            }
        }
        catch (IOException e)
        {
            throw new UpgradeException(e);
        }
    }

    public void setMapping(String mapping)
    {
        this.mappings.clear();
        this.mappings.add(mapping);
    }

    public void setMappings(List<String> mappings)
    {
        this.mappings = mappings;
    }

    /**
     * Required resource.
     *
     * @param dataSource reference
     */
    public void setDataSource(DataSource dataSource)
    {
        this.dataSource = dataSource;
    }

    public void setDatabaseConfig(DatabaseConfig config)
    {
        this.databaseConfig = config;
    }
}
