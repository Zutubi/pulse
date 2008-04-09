package com.zutubi.pulse.restore;

import com.zutubi.pulse.database.DatabaseConfig;
import com.zutubi.pulse.restore.feedback.Feedback;
import com.zutubi.pulse.restore.feedback.FeedbackProvider;
import com.zutubi.pulse.transfer.Table;
import com.zutubi.pulse.transfer.TransferAPI;
import com.zutubi.pulse.transfer.TransferException;
import com.zutubi.pulse.transfer.TransferListener;
import com.zutubi.pulse.upgrade.tasks.MutableConfiguration;
import com.zutubi.pulse.util.JDBCUtils;
import com.zutubi.util.IOUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import javax.sql.DataSource;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 *
 *
 */
public class DatabaseArchive extends AbstractArchivableComponent implements FeedbackProvider
{
    private static final String EXPORT_FILENAME = "export.xml";
    
    private static final String TABLE_FILENAME = "table.properties";
    
    private List<String> mappings = new LinkedList<String>();

    private DatabaseConfig databaseConfig = null;

    private DataSource dataSource = null;
    
    private Feedback feedback;

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
                    throw new ArchiveException("Failed to create new file: " + file.getCanonicalPath());
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
            final Map<String, Long> tableSizes = readTableSizes(new File(base, "tables.properties"));
            long i = 0;
            for (long rowCount : tableSizes.values())
            {
                i += rowCount;
            }
            final long allTablesRowCount = i;
            
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
                transfer.setListener(new TransferListener()
                {
                    private String currentTable = "";
                    private long rowCount = 0;
                    private long tableRowCount = 0;
                    private long rowsCountedSoFar = 0;

                    public void startTable(Table table)
                    {
                        currentTable = table.getName();
                        tableRowCount = tableSizes.get(currentTable);
                    }

                    public void row(Map<String, Object> row)
                    {
                        rowCount++;
                        rowsCountedSoFar++;
                        feedback.setStatusMessage("" + currentTable + ": " + rowCount + "/" + tableRowCount);
                        feedback.setPercetageComplete((int)((100 * rowsCountedSoFar)/ allTablesRowCount));
                    }

                    public void endTable()
                    {
                        rowCount = 0;
                        currentTable = "";
                    }
                });
                transfer.restore(configuration, export, dataSource);
            }
        }
        catch (SQLException e)
        {
            throw new ArchiveException(e);
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

    private Map<String, Long> readTableSizes(File file) throws IOException
    {
        Map<String, Long> tableSizes = new HashMap<String, Long>();

        BufferedReader reader = null;
        try
        {
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
            String line;
            while ((line = reader.readLine()) != null)
            {
                if (line.startsWith("#"))
                {
                    continue;
                }
                int index = line.indexOf('=');
                String tableName = line.substring(0, index);
                Long rowCount = Long.valueOf(line.substring(index + 1));
                tableSizes.put(tableName, rowCount);
            }
        }
        finally
        {
            IOUtils.close(reader);
        }

        return tableSizes;
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

    public void setFeedback(Feedback feedback)
    {
        this.feedback = feedback;
    }
}
