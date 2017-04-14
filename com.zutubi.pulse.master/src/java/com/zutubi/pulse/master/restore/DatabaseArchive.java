/* Copyright 2017 Zutubi Pty Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.zutubi.pulse.master.restore;

import com.google.common.io.ByteSource;
import com.google.common.io.Files;
import com.zutubi.pulse.core.util.JDBCUtils;
import com.zutubi.pulse.master.database.DatabaseConfig;
import com.zutubi.pulse.master.hibernate.MutableConfiguration;
import com.zutubi.pulse.master.transfer.Table;
import com.zutubi.pulse.master.transfer.TransferAPI;
import com.zutubi.pulse.master.transfer.TransferException;
import com.zutubi.pulse.master.transfer.TransferListener;
import com.zutubi.pulse.master.util.monitor.FeedbackAware;
import com.zutubi.pulse.master.util.monitor.TaskFeedback;
import com.zutubi.util.io.IOUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import javax.sql.DataSource;
import java.io.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;

/**
 * An archive wrapper around the transfer api to enable backup and restore of database contents.
 */
public class DatabaseArchive extends AbstractArchiveableComponent implements FeedbackAware
{
    static final String EXPORT_FILENAME = "export.xml";
    static final String TRACKING_FILENAME = "tables.properties";
    
    private List<String> mappings = new ArrayList<>();

    private DataSource dataSource = null;

    private TaskFeedback feedback;

    private Properties hibernatePropeties;

    public String getName()
    {
        return "database";
    }

    public String getDescription()
    {
        return "Restores a snapshot of the database schema and content based on internal Pulse " +
                "schema descriptors.  This process will reconstruct the schema in your " +
                "specified database.  All existing data will be replaced.";
    }

    public void backup(File base) throws ArchiveException
    {
        try
        {
            if (!base.exists() && !base.mkdirs())
            {
                throw new IOException("Failed to create archive output directory.");
            }

            List<Resource> resources = new ArrayList<>();
            for (String mapping : mappings)
            {
                resources.add(new ClassPathResource(mapping));
            }

            // Export the schema as part of the data export.
            for (final Resource resource : resources)
            {
                File file = new File(base, resource.getFilename());
                if (!file.createNewFile())
                {
                    throw new ArchiveException("Failed to create new file: " + file.getCanonicalPath());
                }

                new ByteSource()
                {
                    @Override
                    public InputStream openStream() throws IOException
                    {
                        return resource.getInputStream();
                    }
                }.copyTo(Files.asByteSink(file));
            }

            File export = new File(base, EXPORT_FILENAME);
            MutableConfiguration configuration = new MutableConfiguration();
            for (Resource resource : resources)
            {
                configuration.addInputStream(resource.getInputStream());
            }
            configuration.setProperties(hibernatePropeties);

            final Map<String, Long> transferedTableSizes = new HashMap<>();

            TransferAPI transfer = new TransferAPI();
            transfer.addListener(new LogTableSizeTransferListener(transferedTableSizes));
            transfer.dump(configuration, dataSource, export);

            writeTableSizes(transferedTableSizes, new File(base, TRACKING_FILENAME));
        }
        catch (IOException | TransferException e)
        {
            throw new ArchiveException(e);
        }
    }

    public boolean exists(File dir)
    {
        return getTablesFile(dir).isFile();
    }

    public void restore(File base) throws ArchiveException
    {
        try
        {
            final Map<String, Long> tableSizes = readTableSizes(getTablesFile(base));

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
                transfer.addListener(new FeedbackTransferListener(tableSizes, feedback));
                transfer.restore(configuration, export, dataSource);
            }
        }
        catch (SQLException | IOException | TransferException e)
        {
            throw new ArchiveException(e);
        }
    }

    private File getTablesFile(File base)
    {
        return new File(base, TRACKING_FILENAME);
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
        Map<String, Long> tableSizes = new HashMap<>();

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

    public void setFeedback(TaskFeedback feedback)
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
        private TaskFeedback feedback;
        private long allTablesRowCount = 0;

        public FeedbackTransferListener(Map<String, Long> tableSizes, TaskFeedback feedback)
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
            if (feedback != null)
            {
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
        }

        public void endTable()
        {
            rowCount = 0;
            currentTable = "";
        }

        public void end()
        {
            if (feedback != null)
            {
                feedback.setStatusMessage("finalizing database scheme, applying constraints.");
            }
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
