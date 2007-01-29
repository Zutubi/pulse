package com.zutubi.pulse.upgrade.tasks;

import com.zutubi.pulse.bootstrap.DatabaseConsole;
import com.zutubi.pulse.upgrade.DataSourceAware;
import com.zutubi.pulse.upgrade.UpgradeContext;
import com.zutubi.pulse.upgrade.UpgradeException;
import com.zutubi.pulse.upgrade.UpgradeTask;
import org.springframework.core.io.ClassPathResource;

import javax.sql.DataSource;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

/**
 * <class-comment/>
 */
public class MigrateSchemaUpgradeTask implements DataSourceAware, UpgradeTask
{
    private List<String> mappings = new LinkedList<String>();

    private List<String> errors = new LinkedList<String>();

    protected DataSource dataSource;

    protected DatabaseConsole databaseConsole;

    private int buildNumber;

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

    public void execute(UpgradeContext context) throws UpgradeException
    {
        try
        {
            // manually setup the hibernate configuration
            MutableConfiguration config = new MutableConfiguration();

            // load these properties from the context, same place that all the other
            // properties are defined.
            Properties props = databaseConsole.getConfig().getHibernateProperties();
            props.put("hibernate.connection.provider_class", "com.zutubi.pulse.upgrade.tasks.HackyUpgradeTaskConnectionProvider");

            // slight hack to provide hibernate with access to the configured datasource.
            HackyUpgradeTaskConnectionProvider.dataSource = dataSource;

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

    /**
     * Required resource.
     *
     * @param databaseConsole reference
     */
    public void setDatabaseConsole(DatabaseConsole databaseConsole)
    {
        this.databaseConsole = databaseConsole;
    }
}
