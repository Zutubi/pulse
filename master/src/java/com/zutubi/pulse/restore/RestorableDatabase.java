package com.zutubi.pulse.restore;

import com.zutubi.pulse.upgrade.tasks.MutableConfiguration;
import com.zutubi.pulse.transfer.TransferAPI;
import com.zutubi.pulse.transfer.TransferException;
import com.zutubi.pulse.database.DatabaseConfig;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.LinkedList;

import org.springframework.core.io.Resource;
import org.springframework.core.io.ClassPathResource;

import javax.sql.DataSource;

/**
 *
 *
 */
public class RestorableDatabase implements Restorable
{
    private List<String> mappings = new LinkedList<String>();
    private DatabaseConfig databaseConfig = null;
    private DataSource dataSource = null;

    public String getName()
    {
        return "database";
    }

    public void backup(File base)
    {

    }

    public void restore(File base) throws RestoreException
    {
        try
        {
            File export = new File(base, "export.xml");
            if (export.isFile())
            {
                MutableConfiguration configuration = new MutableConfiguration();
                for (String mapping : mappings)
                {
                    Resource resource = new ClassPathResource(mapping);
                    configuration.addInputStream(resource.getInputStream());
                }

                configuration.setProperties(databaseConfig.getHibernateProperties());

                TransferAPI transfer = new TransferAPI();
                transfer.restore(configuration, export, dataSource);
            }
        }
        catch (IOException e)
        {
            throw new RestoreException(e);
        }
        catch (TransferException e)
        {
            throw new RestoreException(e);
        }
    }

    public void setMappings(List<String> mappings)
    {
        this.mappings = mappings;
    }

    public void setDatabaseConfig(DatabaseConfig databaseConfig)
    {
        this.databaseConfig = databaseConfig;
    }

    public void setDataSource(DataSource dataSource)
    {
        this.dataSource = dataSource;
    }
}
