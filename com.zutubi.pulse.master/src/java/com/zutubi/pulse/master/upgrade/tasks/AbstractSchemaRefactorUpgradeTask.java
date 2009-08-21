package com.zutubi.pulse.master.upgrade.tasks;

import com.zutubi.pulse.master.hibernate.HackyConnectionProvider;
import com.zutubi.pulse.master.hibernate.MutableConfiguration;
import com.zutubi.pulse.master.hibernate.SchemaRefactor;
import org.hibernate.cfg.Environment;

import java.sql.Connection;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

/**
 */
public abstract class AbstractSchemaRefactorUpgradeTask extends DatabaseUpgradeTask
{
    private List<String> mappings = new LinkedList<String>();
    private Properties hibernateProperties;

    public boolean haltOnFailure()
    {
        return true;
    }

    public void execute(Connection con) throws Exception
    {
        Properties props = new Properties();
        props.putAll(hibernateProperties);
        props.put(Environment.CONNECTION_PROVIDER, "com.zutubi.pulse.master.hibernate.HackyConnectionProvider");

        // slight hack to provide hibernate with access to the configured datasource.
        HackyConnectionProvider.dataSource = dataSource;

        // manually setup the hibernate configuration
        MutableConfiguration config = new MutableConfiguration();
        config.addClassPathMappings(mappings);
        config.buildMappings();
        
        SchemaRefactor refactor = new SchemaRefactor(config, props);
        doRefactor(con, refactor);
    }

    protected abstract void doRefactor(Connection con, SchemaRefactor refactor) throws Exception;

    public void setHibernateProperties(Properties hibernateProperties)
    {
        this.hibernateProperties = hibernateProperties;
    }

    /**
     * The hibernate mappings that define the existing schema that we are upgrading from.
     * 
     * @param mappings a list of classpath references to .hbm.xml files.
     */
    public void setMappings(List<String> mappings)
    {
        this.mappings = mappings;
    }
}
