package com.zutubi.pulse.restore;

import com.zutubi.pulse.database.DatabaseConfig;
import com.zutubi.pulse.transfer.TransferAPI;
import com.zutubi.pulse.transfer.TransferException;
import com.zutubi.pulse.upgrade.tasks.MutableConfiguration;
import com.zutubi.pulse.util.JDBCUtils;
import com.zutubi.util.IOUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import javax.sql.DataSource;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

/**
 *
 *
 */
public class DatabaseArchive extends AbstractArchivableComponent
{
    private static final String EXPORT_FILENAME = "export.xml";

    private List<String> mappings = new LinkedList<String>();

    private DatabaseConfig databaseConfig = null;

    private DataSource dataSource = null;

    public String getName()
    {
        return "database";
    }

    public String getDescription()
    {
        return "The database restoration process takes the 1.2.x data export and imports it into your 2.0 database. " +
                "Please be aware that this process will reconstruct the 2.0 schema in your specified database.  " +
                "All existing data will be replaced.";
    }

    public void backup(File base) throws ArchiveException
    {
        try
        {
            if (!base.exists() && !base.mkdirs())
            {
                throw new IOException("Failed to create archive output directory.");
            }

            // need to export the configuration as part of the data export.
            for (String mapping : mappings)
            {
                Resource resource = new ClassPathResource(mapping);
                File file = new File(base, resource.getFilename());
                if (!file.createNewFile())
                {
                    // :|
                    System.out.println("Unexpected.");
                }
                IOUtils.writeToFile(file, resource.getInputStream());
            }

            File export = new File(base, EXPORT_FILENAME);
            MutableConfiguration configuration = new MutableConfiguration();
            for (String mapping : mappings)
            {
                Resource resource = new ClassPathResource(mapping);
                configuration.addInputStream(resource.getInputStream());
            }

            configuration.setProperties(databaseConfig.getHibernateProperties());

            TransferAPI transfer = new TransferAPI();
            transfer.dump(configuration, dataSource, export);
        }
        catch (IOException e)
        {
            throw new ArchiveException(e);
        }
        catch (TransferException e)
        {
            throw new ArchiveException(e);
        }
    }

    public void restore(File base) throws ArchiveException
    {
        try
        {
            File export = new File(base, EXPORT_FILENAME);
            if (export.isFile())
            {
                MutableConfiguration configuration = new MutableConfiguration();

                File[] mappingFiles = base.listFiles(new FilenameFilter()
                {
                    public boolean accept(File dir, String name)
                    {
                        return name.endsWith(".hbm.xml");
                    }
                });

                for (File mappingFile : mappingFiles)
                {
                    // input stream closed by the addInputStream call.
                    configuration.addInputStream(new FileInputStream(mappingFile));
                }

                configuration.setProperties(databaseConfig.getHibernateProperties());

                Connection con = null;
                try
                {
                    con = dataSource.getConnection();
                    JDBCUtils.dropAllTablesFromSchema(con);
                }
                finally
                {
                    JDBCUtils.close(con);
                }

                TransferAPI transfer = new TransferAPI();
                transfer.restore(configuration, export, dataSource);
            }
        }
        catch (SQLException e)
        {
            e.printStackTrace();
            throw new ArchiveException(e);
        }
        catch (IOException e)
        {
            e.printStackTrace();
            throw new ArchiveException(e);
        }
        catch (TransferException e)
        {
            e.printStackTrace();
            throw new ArchiveException(e);
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
