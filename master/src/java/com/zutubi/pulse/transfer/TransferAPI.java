package com.zutubi.pulse.transfer;

import com.zutubi.pulse.Version;
import com.zutubi.util.IOUtils;
import com.zutubi.pulse.util.JDBCUtils;
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

/**
 *
 *
 */
public class TransferAPI
{
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
        XMLTransferTarget target = null;
        try
        {
            if (!isSchemaMappingValid(config, dataSource))
            {
                throw new JDBCTransferException("Schema export aborted due to schema / mapping mismatch");
            }

            target = new XMLTransferTarget();
            target.setOutput(outputStream);
            target.setVersion(Version.getVersion().getBuildNumber());

            JDBCTransferSource source = new JDBCTransferSource();
            source.setConfiguration(config);
            source.setDataSource(dataSource);

            source.transferTo(target);
        }
        finally
        {
            if (target != null)
            {
                target.close();
            }
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
        JDBCTransferTarget target = null;
        try
        {
            target = new JDBCTransferTarget();
            target.setDataSource(dataSource);
            target.setConfiguration(configuration);

            XMLTransferSource source = new XMLTransferSource();
            source.setSource(inputStream);

            source.transferTo(target);
        }
        finally
        {
            if (target != null)
            {
                target.close();
            }
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


}
