package com.zutubi.pulse.migrate;

import com.zutubi.pulse.bootstrap.MasterUserPaths;
import com.zutubi.pulse.database.DatabaseConfig;
import com.zutubi.pulse.monitor.AbstractTask;
import com.zutubi.pulse.monitor.FeedbackAware;
import com.zutubi.pulse.monitor.TaskException;
import com.zutubi.pulse.monitor.TaskFeedback;
import com.zutubi.pulse.transfer.Table;
import com.zutubi.pulse.transfer.TransferAPI;
import com.zutubi.pulse.transfer.TransferException;
import com.zutubi.pulse.transfer.TransferListener;
import com.zutubi.pulse.upgrade.tasks.HibernateUtils;
import com.zutubi.pulse.upgrade.tasks.MutableConfiguration;
import com.zutubi.pulse.util.JDBCUtils;
import org.hibernate.cfg.Configuration;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 *
 *
 */
public class MigrateDatabaseTask extends AbstractTask implements FeedbackAware
{
    private List<String> mappings = new LinkedList<String>();

    private MasterUserPaths userPaths = null;

    private Properties sourceJdbcProperties;

    private Properties targetJdbcProperties;

    private TaskFeedback feedback;

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
            sourceDatabaseConfig.setUserPaths(userPaths);
            DataSource source = sourceDatabaseConfig.createDataSource();

            DatabaseConfig targetDatabaseConfig = new DatabaseConfig(targetJdbcProperties);
            targetDatabaseConfig.setUserPaths(userPaths);
            DataSource target = targetDatabaseConfig.createDataSource();

            MutableConfiguration sourceConfiguration = HibernateUtils.createHibernateConfiguration(mappings, sourceJdbcProperties);
            MutableConfiguration targetConfiguration = HibernateUtils.createHibernateConfiguration(mappings, targetJdbcProperties);

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
        catch (IOException e)
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

                PreparedStatement ps = con.prepareStatement(String.format("SELECT count(*) FROM %s", table.getName()));
                ResultSet rs = ps.executeQuery();
                if (rs.next())
                {
                    tableSizes.put(table.getName(), rs.getLong(1));
                }
                else
                {
                    tableSizes.put(table.getName(), 0L);
                }
            }
        }
        finally
        {
            JDBCUtils.close(con);
        }

        return tableSizes;
    }

    public void setMappings(List<String> mappings)
    {
        this.mappings = mappings;
    }

    public void setSourceJdbcProperties(Properties properties)
    {
        this.sourceJdbcProperties = properties;
    }

    public void setTargetJdbcProperties(Properties properties)
    {
        this.targetJdbcProperties = properties;
    }

    public void setUserPaths(MasterUserPaths userPaths)
    {
        this.userPaths = userPaths;
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
