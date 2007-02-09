package com.zutubi.pulse.upgrade.tasks;

import com.zutubi.pulse.bootstrap.DatabaseConsole;
import com.zutubi.pulse.upgrade.UpgradeContext;
import com.zutubi.pulse.util.JDBCUtils;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

/**
 *
 *
 */
public class MySQLSchemaRefactorUpgradeTask extends DatabaseUpgradeTask
{
    private List<String> mappings = new LinkedList<String>();

    private DatabaseConsole databaseConsole;

    public String getName()
    {
        return "Schema Refactor " + getBuildNumber();
    }

    public String getDescription()
    {
        return "Refactor the schema in preparation for supporting the MySQL database.";
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
        refactor.renameTable("CHANGE", "FILE_CHANGE");
        refactor.renameTable("TRIGGER", "LOCAL_TRIGGER");
        refactor.renameColumn("ARTIFACT", "INDEX", "INDEX_FILE");

        transferUserProperties(con);
    }

    private void transferUserProperties(Connection con) throws SQLException
    {
        CallableStatement stmt = null;
        try
        {
            stmt = con.prepareCall("INSERT INTO user_properties (property_key, property_value, user_id) SELECT key, value, user_id FROM user_props");
            stmt.executeUpdate();
            stmt.close();
            stmt = null;

            stmt = con.prepareCall("DROP TABLE user_props CASCADE");
            stmt.executeUpdate();
        }
        finally
        {
            JDBCUtils.close(stmt);
        }
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
