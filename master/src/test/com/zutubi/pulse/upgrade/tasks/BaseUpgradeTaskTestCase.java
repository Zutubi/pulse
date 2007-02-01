package com.zutubi.pulse.upgrade.tasks;

import com.zutubi.pulse.test.PulseTestCase;
import com.zutubi.pulse.bootstrap.DatabaseConfig;
import org.apache.commons.dbcp.BasicDataSource;
import org.hibernate.cfg.Configuration;
import org.hibernate.tool.hbm2ddl.SchemaUpdate;
import org.springframework.core.io.FileSystemResource;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Properties;
import java.util.List;
import java.util.LinkedList;

/**
 * <class-comment/>
 */
public abstract class BaseUpgradeTaskTestCase extends PulseTestCase
{
    public BaseUpgradeTaskTestCase()
    {
    }

    public BaseUpgradeTaskTestCase(String name)
    {
        super(name);
    }

    protected List<String> getMappings(String build)
    {
        List<String> mappings = new LinkedList<String>();

        String path = "master/src/test/com/zutubi/pulse/upgrade/schema/build_" + build;
        File mappingDir = new File(getPulseRoot(), path);
        for (File f : mappingDir.listFiles(new XMLFilenameFilter()))
        {
            mappings.add("com/zutubi/pulse/upgrade/schema/build_" + build + "/" + f.getName());
        }
        
        return mappings;
    }

/*
    protected void createSchema(BasicDataSource dataSource, DatabaseConfig databaseConfig, String build) throws IOException
    {
        // manually setup the hibernate configuration
        Configuration config = new Configuration();

        // load these properties from the context, same place that all the other
        // properties are defined.
        Properties props = new Properties();
        props.putAll(databaseConfig.getProperties());
        props.put("hibernate.connection.provider_class", "com.zutubi.pulse.upgrade.tasks.HackyUpgradeTaskConnectionProvider");

        // a) retrieve hibernate mappings for schema generation.
        String path = "master/src/test/com/zutubi/pulse/upgrade/schema/build_" + build;
        File mappingDir = new File(getPulseRoot(), path);
        for (File f : mappingDir.listFiles(new XMLFilenameFilter()))
        {
            FileSystemResource r = new FileSystemResource(f);
            config.addInputStream(r.getInputStream());
        }

        // slight hack to provide hibernate with access to the configured datasource.
        HackyUpgradeTaskConnectionProvider.dataSource = dataSource;

        // c) create database
        new SchemaUpdate(config, props).execute(true, true);
    }
*/

    public class XMLFilenameFilter implements FilenameFilter
    {
        public boolean accept(File dir, String name)
        {
            return name.endsWith(".xml");
        }
    }


}
