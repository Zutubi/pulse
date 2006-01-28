package com.cinnamonbob.upgrade.tasks;

import com.cinnamonbob.upgrade.UpgradeTask;
import com.cinnamonbob.upgrade.UpgradeException;
import com.cinnamonbob.upgrade.UpgradeContext;
import org.springframework.core.io.ClassPathResource;
import org.hibernate.tool.hbm2ddl.SchemaUpdate;
import org.hibernate.cfg.Configuration;

import javax.sql.DataSource;
import java.io.IOException;
import java.util.Properties;
import java.util.List;
import java.util.LinkedList;

/**
 * <class-comment/>
 */
public class MigrateSchemaUpgradeTask implements UpgradeTask
{
    private List<String> mappings = new LinkedList<String>();

    private List<String> errors = new LinkedList<String>();

    protected DataSource dataSource;

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

    public List<String> getErrors()
    {
        return errors;
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
            Configuration config = new Configuration();

            // load these properties from the context, same place that all the other
            // properties are defined.
            Properties props = new Properties();
            props.put("hibernate.dialect", "org.hibernate.dialect.HSQLDialect");
            props.put("hibernate.connection.provider_class", "com.cinnamonbob.upgrade.tasks.HackyUpgradeTaskConnectionProvider");

            // slight hack to provide hibernate with access to the configured datasource.
            HackyUpgradeTaskConnectionProvider.dataSource = dataSource;

            // use spring to help load the classpath resources. Rather useful actually.
            for (String mapping : mappings)
            {
                ClassPathResource resource = new ClassPathResource(mapping);
                config.addInputStream(resource.getInputStream());
            }

            // run the schema update.
            new SchemaUpdate(config, props).execute(true, true);
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
     * @param dataSource
     */
    public void setDataSource(DataSource dataSource)
    {
        this.dataSource = dataSource;
    }
}
