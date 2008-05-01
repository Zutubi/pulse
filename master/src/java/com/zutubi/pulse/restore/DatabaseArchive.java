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
import java.io.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Date;
import java.util.Properties;

/**
 * An archive wrapper around the transfer api to enable backup and restore of database contents.
 */
public class DatabaseArchive extends AbstractArchivableComponent implements FeedbackProvider
{
    private static final String EXPORT_FILENAME = "export.xml";

    private List<String> mappings = new LinkedList<String>();

    private DataSource dataSource = null;

    private Feedback feedback;

    private Properties hibernatePropeties;

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

            List<Resource> resources = new LinkedList<Resource>();
            for (String mapping : mappings)
            {
                resources.add(new ClassPathResource(mapping));
            }

            // need to export the configuration as part of the data export.
            for (Resource resource : resources)
            {
                File file = new File(base, resource.getFilename());
                if (!file.createNewFile())
                {
                    throw new ArchiveException("Failed to create new file: " + file.getCanonicalPath());
                }
                IOUtils.writeToFile(file, resource.getInputStream());
            }

            File export = new File(base, EXPORT_FILENAME);
            MutableConfiguration configuration = new MutableConfiguration();
            for (Resource resource : resources)
            {
                configuration.addInputStream(resource.getInputStream());
            }
            configuration.setProperties(hibernatePropeties);

            final Map<String, Long> transferedTableSizes = new HashMap<String, Long>();

            TransferAPI transfer = new TransferAPI();
            transfer.setListener(new LogTableSizeTransferListener(transferedTableSizes));
            transfer.dump(configuration, dataSource, export);

            writeTableSizes(transferedTableSizes, new File(base, "tables.properties"));

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

                configuration.setProperties(hibernatePropeties);

                // clean out the existing database tables to that the import can be successful.
                //TODO: use the mappings file to define which tables to drop to that we are a little more
                //TODO: considerate of the existing content of the database.
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
                transfer.setListener(new FeedbackTransferListener(tableSizes, feedback));
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

    private void writeTableSizes(Map<String, Long> tableSizes, File file) throws IOException
    {
        BufferedWriter writer = null;
        try
        {
            writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "8859_1"));
            writeln(writer, "#" + new Date().toString());
            for (Map.Entry<String, Long> entry : tableSizes.entrySet())
            {
                writeln(writer, entry.getKey() + "=" + entry.getValue());
            }
            writer.flush();
        }
        finally
        {
            IOUtils.close(writer);
        }
    }

    private void writeln(BufferedWriter writer, String s) throws IOException
    {
        writer.write(s);
        writer.newLine();
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
        this.hibernatePropeties = databaseConfig.getHibernateProperties();
    }

    public void setHibernateProperties(Properties props)
    {
        this.hibernatePropeties = props;
    }

    public void setDataSource(DataSource dataSource)
    {
        this.dataSource = dataSource;
    }

    public void setFeedback(Feedback feedback)
    {
        this.feedback = feedback;
    }

    public static class FeedbackTransferListener implements TransferListener
    {
        private String currentTable = "";
        private long rowCount = 0;
        private long tableRowCount = 0;
        private long rowsCountedSoFar = 0;

        private Map<String, Long> tableSizes;
        private Feedback feedback;
        private long allTablesRowCount = 0;

        public FeedbackTransferListener(Map<String, Long> tableSizes, Feedback feedback)
        {
            this.tableSizes = tableSizes;
            this.feedback = feedback;

            for (long rowCount : tableSizes.values())
            {
                allTablesRowCount += rowCount;
            }
        }

        public void start()
        {

        }

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
            int percentageComplete = (int) ((100 * rowsCountedSoFar) / allTablesRowCount);
            if (percentageComplete < 100)
            {
                feedback.setPercetageComplete(percentageComplete);
            }
            else
            {
                // will leaving the feedback at 99 cause a problem?.. if so, we will need a hook to
                // tell us when the processing is complete so that we can set it to 100.
                feedback.setPercetageComplete(99);
            }
        }

        public void endTable()
        {
            rowCount = 0;
            currentTable = "";
        }

        public void end()
        {
            feedback.setStatusMessage("finalizing database scheme, applying constraints.");
        }
    }

    public static class LogTableSizeTransferListener implements TransferListener
    {
        private long rowCount = 0;
        private String currentTableName;
        private Map<String, Long> transferedTableSizes;

        public LogTableSizeTransferListener(Map<String, Long> transferedTableSizes)
        {
            this.transferedTableSizes = transferedTableSizes;
        }

        public void start()
        {

        }

        public void startTable(Table table)
        {
            rowCount = 0;
            currentTableName = table.getName();
            transferedTableSizes.put(currentTableName, rowCount);
        }

        public void row(Map<String, Object> row)
        {
            rowCount++;
        }

        public void endTable()
        {
            transferedTableSizes.put(currentTableName, rowCount);
        }

        public void end()
        {

        }
    }
}
