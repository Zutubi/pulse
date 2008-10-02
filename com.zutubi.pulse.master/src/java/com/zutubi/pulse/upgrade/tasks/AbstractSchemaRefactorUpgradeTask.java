package com.zutubi.pulse.upgrade.tasks;

import com.zutubi.pulse.master.database.DatabaseConsole;
import com.zutubi.pulse.master.hibernate.HackyConnectionProvider;
import com.zutubi.pulse.master.hibernate.MutableConfiguration;
import com.zutubi.pulse.master.hibernate.SchemaRefactor;
import org.hibernate.cfg.Environment;

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
        props.put(Environment.CONNECTION_PROVIDER, "com.zutubi.pulse.master.hibernate.HackyConnectionProvider");

        // slight hack to provide hibernate with access to the configured datasource.
        HackyConnectionProvider.dataSource = dataSource;

        config.addClassPathMappings(mappings);
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
