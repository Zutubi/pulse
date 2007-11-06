package com.zutubi.pulse.upgrade.tasks;

import com.zutubi.pulse.bootstrap.DatabaseConsole;
import com.zutubi.pulse.upgrade.UpgradeContext;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

/**
 */
public abstract class AbstractSchemaRefactorUpgradeTask extends DatabaseUpgradeTask
{
    private List<String> mappings = new LinkedList<String>();
    private DatabaseConsole databaseConsole;

    public boolean haltOnFailure()
    {
        return true;
    }

    public void execute(Connection con) throws IOException, SQLException
    {
        // manually setup the hibernate configuration
        MutableConfiguration config = new MutableConfiguration();

        // load these properties from the context, same place that all the other
        // properties are defined.
        Properties props = databaseConsole.getConfig().getHibernateProperties();
        props.put("hibernate.connection.provider_class", "com.zutubi.pulse.upgrade.tasks.HackyConnectionProvider");

        // slight hack to provide hibernate with access to the configured datasource.
        HackyConnectionProvider.dataSource = dataSource;

        for (String mapping : mappings)
        {
            ClassPathResource resource = new ClassPathResource(mapping);
            config.addInputStream(resource.getInputStream());
        }

        config.buildMappings();
        SchemaRefactor refactor = new SchemaRefactor(config, props);
        doRefactor(con, refactor);
    }

    protected abstract void doRefactor(Connection con, SchemaRefactor refactor) throws SQLException, IOException;

    public void setDatabaseConsole(DatabaseConsole databaseConsole)
    {
        this.databaseConsole = databaseConsole;
    }

    public void setMappings(List<String> mappings)
    {
        this.mappings = mappings;
    }
}
