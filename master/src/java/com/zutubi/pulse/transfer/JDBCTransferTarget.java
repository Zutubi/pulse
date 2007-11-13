package com.zutubi.pulse.transfer;

import com.zutubi.pulse.util.JDBCUtils;
import com.zutubi.pulse.util.JDBCTypes;
import com.zutubi.util.logging.Logger;
import org.hibernate.cfg.Configuration;
import org.hibernate.dialect.Dialect;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 *
 *
 */
public class JDBCTransferTarget implements TransferTarget
{
    private static final Logger LOG = Logger.getLogger(JDBCTransferTarget.class);

    private Table table;

    private Connection connection;
    private PreparedStatement insert;
    private String insertSql;
    private DataSource dataSource;
    private Configuration configuration;

    private boolean originalAutoCommitSetting = false;
    private int rowCount = 0;

    public void start() throws TransferException
    {
        try
        {
            connection = dataSource.getConnection();
            createSchema(configuration);

            originalAutoCommitSetting = connection.getAutoCommit();
            connection.setAutoCommit(false);
        }
        catch (SQLException e)
        {
            throw new JDBCTransferException(e);
        }
    }

    public void startTable(Table table) throws TransferException
    {
        try
        {
            this.table = table;

            // Check that table somewhat matches the schemaTable it will be inserted into.  This verifies
            // that the data from the transfer source tables matches the data for this transfer target.
            org.hibernate.mapping.Table tableMapping = getTableMapping(table.getName());

            for (Column column : table.getColumns())
            {
                // get mapped column.
                org.hibernate.mapping.Column columnMapping = getColumnMapping(tableMapping, column.getName());
                if (columnMapping == null)
                {
                    throw new TransferException("Transfer target does not contain column " + column.getName() + " in table " + table.getName());
                }
                if (columnMapping.getSqlTypeCode() != column.getSqlTypeCode())
                {
                    throw new TransferException("Column type mismatch for column " + column.getName() + ". " +
                            "Export contains type " + JDBCTypes.toString(column.getSqlTypeCode()) + " " +
                            "but we expected type " + JDBCTypes.toString(columnMapping.getSqlTypeCode()));
                }
            }

            insertSql = MappingUtils.sqlInsert(table);
            LOG.info(insertSql);
            insert = connection.prepareStatement(insertSql);
        }
        catch (SQLException e)
        {
            throw new JDBCTransferException(e);
        }
    }

    public void row(Map<String, Object> row) throws TransferException
    {
        try
        {
            List<Column> columns = table.getColumns();
            List<Object> data = new LinkedList<Object>();
            for (int i = 0; i < columns.size(); i++)
            {
                Object obj = row.get(columns.get(i).getName());
                data.add(obj);
                insert.setObject(i + 1, obj);
            }
            String sql = insertSql.replace("?", "%s");
            LOG.fine(String.format(sql, data.toArray()));
            insert.execute();

            if (++rowCount % 1000 == 0)
            {
                connection.commit();
            }
        }
        catch (SQLException e)
        {
            throw new JDBCTransferException(e);
        }
    }

    public void endTable() throws TransferException
    {
        try
        {
            connection.commit();
        }
        catch (SQLException e)
        {
            throw new JDBCTransferException(e);
        }
        JDBCUtils.close(insert);
    }

    public void end() throws TransferException
    {
        try
        {
            connection.setAutoCommit(originalAutoCommitSetting);

            createSchemaConstraints(configuration);
        }
        catch (SQLException e)
        {
            throw new JDBCTransferException(e);
        }
    }

    public void close()
    {
        JDBCUtils.close(connection);
    }

    public void setDataSource(DataSource dataSource)
    {
        this.dataSource = dataSource;
    }

    public void setConfiguration(Configuration configuration)
    {
        this.configuration = configuration;
    }

    private void createSchema(Configuration configuration) throws SQLException, TransferException
    {
        // check if the schema exists. If so, generate an appropriate error.
        Iterator tableMappings = configuration.getTableMappings();
        while (tableMappings.hasNext())
        {
            org.hibernate.mapping.Table table = (org.hibernate.mapping.Table) tableMappings.next();
            if (JDBCUtils.tableExists(connection, table.getName()))
            {
                throw new JDBCTransferException("Unable to create the new database schema. The table '" + table.getName() + "' " +
                        "already exists. Please ensure that you are importing into a blank database.");
            }
        }

        Dialect dialect = Dialect.getDialect(configuration.getProperties());
        String[] sqlCreate = configuration.generateSchemaCreationScript(dialect);
        Statement stmt = null;
        try
        {
            stmt = connection.createStatement();
            for (String sql : sqlCreate)
            {
                if (sql.startsWith("create"))
                {
                    LOG.info(sql);
                    stmt.executeUpdate(sql);
                }
            }
        }
        finally
        {
            JDBCUtils.close(stmt);
        }
    }

    private void createSchemaConstraints(Configuration configuration) throws SQLException
    {
        Dialect dialect = Dialect.getDialect(configuration.getProperties());
        String[] sqlAlter = configuration.generateSchemaCreationScript(dialect);
        Statement stmt = null;
        try
        {
            stmt = connection.createStatement();
            for (String sql : sqlAlter)
            {
                if (sql.startsWith("alter"))
                {
                    LOG.info(sql);
                    stmt.executeUpdate(sql);
                }
            }
        }
        finally
        {
            JDBCUtils.close(stmt);
        }
    }

    private org.hibernate.mapping.Table getTableMapping(String tableName)
    {
        Iterator tables = configuration.getTableMappings();
        while (tables.hasNext())
        {
            org.hibernate.mapping.Table table = (org.hibernate.mapping.Table) tables.next();
            if (table.getName().equals(tableName))
            {
                return table;
            }
        }
        if (HibernateUniqueKeyTable.isTable(tableName))
        {
            return HibernateUniqueKeyTable.getMapping();
        }
        return null;
    }

    private org.hibernate.mapping.Column getColumnMapping(org.hibernate.mapping.Table table, String columnName)
    {
        Iterator iterator = table.getColumnIterator();
        while (iterator.hasNext())
        {
            org.hibernate.mapping.Column column = (org.hibernate.mapping.Column) iterator.next();
            if (column.getName().equals(columnName))
            {
                return column;
            }
        }
        return null;
    }
}
