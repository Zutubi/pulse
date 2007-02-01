package com.zutubi.pulse.upgrade.tasks;

import com.zutubi.pulse.upgrade.UpgradeContext;
import com.zutubi.pulse.bootstrap.DatabaseConsole;

import java.sql.Connection;
import java.sql.SQLException;
import java.io.IOException;
import java.util.Properties;
import java.util.List;
import java.util.LinkedList;

import org.springframework.core.io.ClassPathResource;

/**
 *
 *
 */
public class PostgresSchemaRefactorUpgradeTask extends DatabaseUpgradeTask
{
    private List<String> mappings = new LinkedList<String>();

    private DatabaseConsole databaseConsole;

    public String getName()
    {
        return "Schema Refactor " + getBuildNumber();
    }

    public String getDescription()
    {
        return "Refactor the schema in preparation for supporting the postgres database.";
    }

    public boolean haltOnFailure()
    {
        return true;
    }

    public void execute(UpgradeContext context, Connection con) throws IOException, SQLException
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

        SchemaRefactor refactor = new SchemaRefactor(config, props);
        refactor.renameTable("USER", "LOCAL_USER");
        refactor.renameColumn("CLEANUP_RULE", "limit", "RULE_LIMIT");
        refactor.renameColumn("BUILD_RESULT", "USER", "LOCAL_USER");
        refactor.renameColumn("BUILD_REASON", "USER", "LOCAL_USER");
    }

    public void setDatabaseConsole(DatabaseConsole databaseConsole)
    {
        this.databaseConsole = databaseConsole;
    }

    public void setMappings(List<String> mappings)
    {
        this.mappings = mappings;
    }
}
