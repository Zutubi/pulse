package com.zutubi.pulse.transfer;

import com.zutubi.pulse.Version;
import com.zutubi.pulse.util.JDBCUtils;
import com.zutubi.util.CollectionUtils;
import com.zutubi.util.IOUtils;
import com.zutubi.util.UnaryFunction;
import org.hibernate.cfg.Configuration;
import org.hibernate.mapping.Column;
import org.hibernate.mapping.Table;

import javax.sql.DataSource;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 *
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
                throw new JDBCTransferException("Schema export aborted due to schema / mapping mismatch");
            }

            XMLTransferTarget xmlTarget = new XMLTransferTarget();
            xmlTarget.setOutput(outputStream);
            xmlTarget.setVersion(Version.getVersion().getBuildNumber());

            JDBCTransferSource source = new JDBCTransferSource();
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

    private void close(TransferTarget target)
    {
        if (target != null)
        {
            target.close();
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
            JDBCTransferTarget jdbcTarget = new JDBCTransferTarget();
            jdbcTarget.setDataSource(dataSource);
            jdbcTarget.setConfiguration(configuration);

            XMLTransferSource source = new XMLTransferSource();
            source.setSource(inputStream);

            target = wrapTargetIfNecessary(jdbcTarget);

            source.transferTo(target);
        }
        finally
        {
            close(target);
        }
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

    public void addListener(TransferListener listener)
    {
        if (listeners == null)
        {
            listeners = new LinkedList<TransferListener>();
        }
        listeners.add(listener);
    }

    /**
     * 
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
            CollectionUtils.traverse(listeners, new UnaryFunction<TransferListener>()
            {
                public void process(TransferListener transferListener)
                {
                    transferListener.start();
                }
            });
            delegate.start();
        }

        public void startTable(final com.zutubi.pulse.transfer.Table table) throws TransferException
        {
            CollectionUtils.traverse(listeners, new UnaryFunction<TransferListener>()
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
            CollectionUtils.traverse(listeners, new UnaryFunction<TransferListener>()
            {
                public void process(TransferListener transferListener)
                {
                    transferListener.row(row);
                }
            });
            delegate.row(row);
        }

        public void endTable() throws TransferException
        {
            CollectionUtils.traverse(listeners, new UnaryFunction<TransferListener>()
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
            CollectionUtils.traverse(listeners, new UnaryFunction<TransferListener>()
            {
                public void process(TransferListener transferListener)
                {
                    transferListener.end();
                }
            });
            delegate.end();
        }

        public void close()
        {
            delegate.close();
        }
    }
}
