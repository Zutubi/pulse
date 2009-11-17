package com.zutubi.pulse.master.transfer;

import com.zutubi.pulse.Version;
import com.zutubi.pulse.core.util.JDBCUtils;
import com.zutubi.pulse.master.transfer.jdbc.HibernateTransferException;
import com.zutubi.pulse.master.transfer.jdbc.HibernateTransferSource;
import com.zutubi.pulse.master.transfer.jdbc.HibernateTransferTarget;
import com.zutubi.pulse.master.transfer.jdbc.MappingUtils;
import com.zutubi.pulse.master.transfer.xml.XMLTransferSource;
import com.zutubi.pulse.master.transfer.xml.XMLTransferTarget;
import com.zutubi.util.CollectionUtils;
import com.zutubi.util.UnaryProcedure;
import com.zutubi.util.io.IOUtils;
import org.hibernate.cfg.Configuration;
import org.hibernate.mapping.Column;
import org.hibernate.mapping.Table;

import javax.sql.DataSource;
import java.io.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * The TransferAPI is the entry point for the transfer package, providing methods that
 * transfer data from jdbc -> xml file, xml file -> jdbc and jdbc -> jdbc.
 *
 */
public class TransferAPI
{
    private List<TransferListener> listeners;

    public void dump(Configuration config, DataSource dataSource, File outFile) throws TransferException
    {
        FileOutputStream outputStream = null;
        try
        {
            outputStream = new FileOutputStream(outFile);

            dump(config, dataSource, outputStream);

        }
        catch (IOException e)
        {
            throw new TransferException(e);
        }
        finally
        {
            IOUtils.close(outputStream);
        }
    }

    public void dump(Configuration config, DataSource dataSource, OutputStream outputStream) throws TransferException
    {
        TransferTarget target = null;
        try
        {
            if (!isSchemaMappingValid(config, dataSource))
            {
                throw new HibernateTransferException("Schema export aborted due to schema / mapping mismatch");
            }

            XMLTransferTarget xmlTarget = new XMLTransferTarget();
            xmlTarget.setOutput(outputStream);
            xmlTarget.setVersion(Version.getVersion().getBuildNumber());

            HibernateTransferSource source = new HibernateTransferSource();
            source.setConfiguration(config);
            source.setDataSource(dataSource);

            target = wrapTargetIfNecessary(xmlTarget);

            source.transferTo(target);
        }
        finally
        {
            close(target);
        }
    }

    public void restore(Configuration configuration, File inFile, DataSource dataSource) throws TransferException
    {
        FileInputStream inputStream = null;
        try
        {
            inputStream = new FileInputStream(inFile);
            restore(configuration, dataSource, inputStream);
        }
        catch (IOException e)
        {
            throw new TransferException(e);
        }
        finally
        {
            IOUtils.close(inputStream);
        }
    }

    public void restore(Configuration configuration, DataSource dataSource, InputStream inputStream) throws TransferException
    {
        // configure the import.
        TransferTarget target = null;
        try
        {
            //TODO: in the same way that the dump verifies the configuration against the datasource,
            //TODO: it would be good if we could verify the configuration against the input stream to catch
            //TODO: any mismatches early.

            XMLTransferSource source = new XMLTransferSource();
            source.setSource(inputStream);

            HibernateTransferTarget hibernateTarget = new HibernateTransferTarget();
            hibernateTarget.setDataSource(dataSource);
            hibernateTarget.setConfiguration(configuration);

            target = wrapTargetIfNecessary(hibernateTarget);

            source.transferTo(target);
        }
        finally
        {
            close(target);
        }
    }

    /**
     * Migrate the contents of the dataSource to the dataTarget.
     *
     * @param sourceConfiguration of the source data.
     * @param targetConfiguration of the target data.
     *
     * @param dataSource the source DataSource connection.
     * @param dataTarget the target DataSource connection.
     *
     * @throws TransferException if there are any problems with the migration that would
     * result in a difference in the source and target representations of the data.
     */
    public void migrate(Configuration sourceConfiguration, DataSource dataSource, Configuration targetConfiguration, DataSource dataTarget) throws TransferException
    {
        TransferTarget target = null;
        try
        {
            HibernateTransferTarget hibernateTarget = new HibernateTransferTarget();
            hibernateTarget.setDataSource(dataTarget);
            hibernateTarget.setConfiguration(targetConfiguration);

            HibernateTransferSource hibernateSource = new HibernateTransferSource();
            hibernateSource.setConfiguration(sourceConfiguration);
            hibernateSource.setDataSource(dataSource);

            target = wrapTargetIfNecessary(hibernateTarget);

            hibernateSource.transferTo(target);
        }
        finally
        {
            close(target);
        }
    }

    /**
     * Add a transfer listener to the API to receive feedback on the processing of a dump, restore or migrate
     * opperation.
     *
     * @param listener instance that will receive the callbacks.
     */
    public void addListener(TransferListener listener)
    {
        if (listeners == null)
        {
            listeners = new LinkedList<TransferListener>();
        }
        listeners.add(listener);
    }

    private TransferTarget wrapTargetIfNecessary(TransferTarget target)
    {
        if (listeners != null && listeners.size() > 0)
        {
            return new InterceptorTransferTarget(target, listeners);
        }
        else
        {
            return  target;
        }
    }

    private void close(TransferTarget target) throws TransferException
    {
        if (target != null)
        {
            target.close();
        }
    }

    /**
     * Run some diagnostics on the connected database, checking that all of the tables and columns in our mapping
     * are present and accounted for.
     *
     * @param configuration the hibernate schema mappings.
     * @param dataSource    the datasource for the database being verified.
     */
    private boolean isSchemaMappingValid(Configuration configuration, DataSource dataSource) throws TransferException
    {
        Connection con = null;
        try
        {
            try
            {
                con = dataSource.getConnection();
            }
            catch (SQLException e)
            {
                throw new TransferException("Failed to connect to the configured dataSource.  Cause: '"+e.getMessage()+"'");
            }

            // we could use the configuration.validateSchema() except that the output is not as friendly or comprehensive
            // as we would like - reporting only first error that it encounters.  The downside to the custom implementation
            // is that it is not as comprehensive in its tests.

            // output diagnostics.
            StringBuilder builder = new StringBuilder();
            builder.append("The following expected schema entities are missing from the source database:\n");

            boolean problemDetected = false;
            boolean noTablesDetected = true;
            Iterator tables = configuration.getTableMappings();
            while (tables.hasNext())
            {
                Table table = (Table) tables.next();
                String tableName = table.getName();
                if (!JDBCUtils.tableExists(con, tableName))
                {
                    // table is missing.
                    builder.append("  Table ").append(tableName).append(" is missing\n");
                    problemDetected = true;
                    continue;
                }

                noTablesDetected = false;

                List<Column> missingColumns = new LinkedList<Column>();
                List<Column> columns = MappingUtils.getColumns(table);
                for (Column column : columns)
                {
                    String columnName = column.getName();
                    if (!JDBCUtils.columnExists(con, tableName, columnName))
                    {
                        missingColumns.add(column);
                        problemDetected = true;
                    }
                }
                if (missingColumns.size() > 0)
                {
                    builder.append("  Table ").append(tableName).append(" is missing column(s): ");
                    String sep = "";
                    for (Column column : missingColumns)
                    {
                        builder.append(sep).append(column.getName());
                        sep = ", ";
                    }
                    builder.append("\n");
                }
            }
            if (problemDetected)
            {
                if (noTablesDetected)
                {
                    System.err.println("The pulse schema could not be located. " +
                            "Please check that your database configuration details are correct.");
                }
                else
                {
                    System.err.println(builder.toString());
                }
            }
            return !problemDetected;
        }
        finally
        {
            JDBCUtils.close(con);
        }
    }

    /**
     * A delegating transfer target that is used to provide the necessary callbacks to the
     * registered transfer listeners. 
     */
    private class InterceptorTransferTarget implements TransferTarget
    {
        private List<TransferListener> listeners;
        private TransferTarget delegate;

        public InterceptorTransferTarget(TransferTarget delegate, List<TransferListener> listeners)
        {
            this.listeners= listeners;
            this.delegate = delegate;
        }

        public void start() throws TransferException
        {
            CollectionUtils.traverse(listeners, new UnaryProcedure<TransferListener>()
            {
                public void process(TransferListener transferListener)
                {
                    transferListener.start();
                }
            });
            delegate.start();
        }

        public void startTable(final com.zutubi.pulse.master.transfer.Table table) throws TransferException
        {
            CollectionUtils.traverse(listeners, new UnaryProcedure<TransferListener>()
            {
                public void process(TransferListener transferListener)
                {
                    transferListener.startTable(table);
                }
            });
            delegate.startTable(table);
        }

        public void row(final Map<String, Object> row) throws TransferException
        {
            CollectionUtils.traverse(listeners, new UnaryProcedure<TransferListener>()
            {
                public void process(TransferListener transferListener)
                {
                    transferListener.row(row);
                }
            });
            delegate.row(row);
        }

        //QUESTION: should the end and endTable callbacks happen before or after the
        //          delegate.endTable and delegate.end calls?

        public void endTable() throws TransferException
        {
            CollectionUtils.traverse(listeners, new UnaryProcedure<TransferListener>()
            {
                public void process(TransferListener transferListener)
                {
                    transferListener.endTable();
                }
            });
            delegate.endTable();
        }

        public void end() throws TransferException
        {
            CollectionUtils.traverse(listeners, new UnaryProcedure<TransferListener>()
            {
                public void process(TransferListener transferListener)
                {
                    transferListener.end();
                }
            });
            delegate.end();
        }

        public void close() throws TransferException
        {
            delegate.close();
        }
    }
}
