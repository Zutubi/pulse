package com.zutubi.pulse.migrate;

import com.zutubi.pulse.database.DatabaseConfig;
import com.zutubi.pulse.monitor.AbstractTask;
import com.zutubi.pulse.monitor.FeedbackAware;
import com.zutubi.pulse.monitor.TaskException;
import com.zutubi.pulse.monitor.TaskFeedback;
import com.zutubi.pulse.transfer.Table;
import com.zutubi.pulse.transfer.TransferAPI;
import com.zutubi.pulse.transfer.TransferException;
import com.zutubi.pulse.transfer.TransferListener;
import com.zutubi.pulse.transfer.jdbc.HibernateUniqueKeyTable;
import com.zutubi.pulse.hibernate.MutableConfiguration;
import com.zutubi.pulse.core.util.JDBCUtils;
import org.hibernate.cfg.Configuration;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

/**
 *
 *
 */
public class MigrateDatabaseTask extends AbstractTask implements FeedbackAware
{
    private Properties sourceJdbcProperties;

    private Properties targetJdbcProperties;

    private TaskFeedback feedback;

    private MutableConfiguration hibernateConfiguration;

    protected MigrateDatabaseTask(String name)
    {
        super(name);
    }

    public void setFeedback(TaskFeedback feedback)
    {
        this.feedback = feedback;
    }


    public boolean haltOnFailure()
    {
        return true;
    }

    public void execute() throws TaskException
    {
        // migrate the database

        // should this include changing over the database.properties? - probably not since this is an external configuration...

        try
        {
            DatabaseConfig sourceDatabaseConfig = new DatabaseConfig(sourceJdbcProperties);
            DataSource source = sourceDatabaseConfig.createDataSource();

            DatabaseConfig targetDatabaseConfig = new DatabaseConfig(targetJdbcProperties);
            DataSource target = targetDatabaseConfig.createDataSource();

            MutableConfiguration sourceConfiguration = hibernateConfiguration.copy();
            sourceConfiguration.setHibernateDialect(sourceJdbcProperties);

            MutableConfiguration targetConfiguration = hibernateConfiguration.copy();
            targetConfiguration.setHibernateDialect(targetJdbcProperties);

            TransferAPI transfer = new TransferAPI();

            // step a, calculate the amount of data that needs to be transfered, -> # rows.

            if (feedback != null)
            {
                Map<String, Long> tableSizes = calculateTableSizes(source, sourceConfiguration);
                transfer.addListener(new MigrateTransferListener(tableSizes, feedback));
            }

            transfer.migrate(sourceConfiguration, source, targetConfiguration, target);

        }
        catch (TransferException e)
        {
            throw new TaskException(e);
        }
        catch (SQLException e)
        {
            throw new TaskException(e);
        }
    }

    private Map<String, Long> calculateTableSizes(DataSource dataSource, Configuration configuration) throws SQLException
    {
        Map<String, Long> tableSizes = new HashMap<String, Long>();

        Connection con = null;
        try
        {
            con = dataSource.getConnection();

            Iterator tables = configuration.getTableMappings();
            while (tables.hasNext())
            {
                org.hibernate.mapping.Table table = (org.hibernate.mapping.Table) tables.next();

                tableSizes.put(table.getName(), countTableSize(table, con));
            }

            org.hibernate.mapping.Table table = HibernateUniqueKeyTable.getMapping();
            tableSizes.put(table.getName(), countTableSize(table, con));
        }
        finally
        {
            JDBCUtils.close(con);
        }

        return tableSizes;
    }

    private long countTableSize(org.hibernate.mapping.Table table, Connection con) throws SQLException
    {
        PreparedStatement ps = con.prepareStatement(String.format("SELECT count(*) FROM %s", table.getName()));
        ResultSet rs = ps.executeQuery();
        if (rs.next())
        {
            return rs.getLong(1);
        }
        else
        {
            return 0L;
        }
    }

    public void setHibernateConfiguration(MutableConfiguration configuration)
    {
        this.hibernateConfiguration = configuration;
    }

    public void setSourceJdbcProperties(Properties properties)
    {
        this.sourceJdbcProperties = properties;
    }

    public void setTargetJdbcProperties(Properties properties)
    {
        this.targetJdbcProperties = properties;
    }

    private class MigrateTransferListener implements TransferListener
    {
        private String currentTableName = "";
        private long currentTableRowCount = 0;
        private long tableRowCount = 0;
        private long rowsCountedSoFar = 0;

        private Map<String, Long> tableSizes;
        private TaskFeedback feedback;
        private long allTablesRowCount = 0;

        public MigrateTransferListener(Map<String, Long> tableSizes, TaskFeedback feedback)
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
            currentTableName = table.getName();
            if (tableSizes.containsKey(currentTableName))
            {
                tableRowCount = tableSizes.get(currentTableName);
            }
            else
            {
                tableRowCount = -1;
            }
        }

        public void row(Map<String, Object> row)
        {
            currentTableRowCount++;
            rowsCountedSoFar++;

            feedback.setStatusMessage(String.format("%s: %s/%s", currentTableName, currentTableRowCount, (tableRowCount == -1) ? "unknown" : tableRowCount));

            int percentageComplete = (int) ((100 * rowsCountedSoFar) / allTablesRowCount);
            if (percentageComplete < 100)
            {
                feedback.setPercetageComplete(percentageComplete);
            }
            else
            {
                // The last percent is reserved for the application of the constraints to the
                // migrated data.  This may take some time, and looks rather awkward if the
                // monitor says 100% yet is not finished.
                // TODO: there needs to be a better location for this type of logic.  Maybe in the
                // TODO: monitor itself, - do not tick over to 100% unless isComplete is true?.
                feedback.setPercetageComplete(99);
            }
        }

        public void endTable()
        {
            currentTableRowCount = 0;
            currentTableName = "";
        }

        public void end()
        {
            if (feedback != null)
            {
                feedback.setStatusMessage("finalizing database scheme, applying constraints.");
            }
        }
    }
}
