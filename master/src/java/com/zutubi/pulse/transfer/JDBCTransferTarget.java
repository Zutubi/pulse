package com.zutubi.pulse.transfer;

import com.zutubi.pulse.upgrade.tasks.MutableConfiguration;
import com.zutubi.pulse.util.JDBCUtils;
import com.zutubi.pulse.util.logging.Logger;
import org.hibernate.dialect.Dialect;
import org.hibernate.mapping.Column;
import org.hibernate.mapping.Table;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Iterator;

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
    private MutableConfiguration configuration;

    public void start() throws TransferException
    {
        try
        {
            connection = dataSource.getConnection();
            createSchema(configuration);
        }
        catch (SQLException e)
        {
            throw new TransferException(e);
        }
    }

    public void startTable(Table table) throws TransferException
    {
        try
        {
            this.table = table;
            insertSql = MappingUtils.sqlInsert(table);
            LOG.info(insertSql);
            insert = connection.prepareStatement(insertSql);
        }
        catch (SQLException e)
        {
            throw new TransferException(e);
        }
    }

    public void row(Map<String, Object> row) throws TransferException
    {
        try
        {
            List<Column> columns = MappingUtils.getColumns(table);
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
        }
        catch (SQLException e)
        {
            throw new TransferException(e);
        }
    }

    public void endTable()
    {
        JDBCUtils.close(insert);
    }

    public void end() throws TransferException
    {
        try
        {
            createSchemaConstraints(configuration);
        }
        catch (SQLException e)
        {
            throw new TransferException(e);
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

    public void setConfiguration(MutableConfiguration configuration)
    {
        this.configuration = configuration;
    }

    private void createSchema(MutableConfiguration configuration) throws SQLException, TransferException
    {
        // check if the schema exists. If so, generate an appropriate error.
        Iterator tableMappings = configuration.getTableMappings();
        while (tableMappings.hasNext())
        {
            Table table = (Table) tableMappings.next();
            if (JDBCUtils.tableExists(connection, table.getName()))
            {
                throw new TransferException("Unable to create the new database schema. The table '"+table.getName()+"' " +
                        "already exists. Please ensure that you are importing into a blank database.");
            }
        }

        Dialect dialect = Dialect.getDialect(configuration.getProperties());
        String[] sqlCreate = configuration.generateSchemaCreationScript(dialect);
        for (String sql : sqlCreate)
        {
            if (sql.startsWith("create"))
            {
                LOG.info(sql);
                JDBCUtils.execute(connection, sql);
            }
        }
    }

    private void createSchemaConstraints(MutableConfiguration configuration) throws SQLException
    {
        Dialect dialect = Dialect.getDialect(configuration.getProperties());
        String[] sqlAlter = configuration.generateSchemaCreationScript(dialect);
        for (String sql : sqlAlter)
        {
            if (sql.startsWith("alter"))
            {
                LOG.info(sql);
                JDBCUtils.execute(connection, sql);
            }
        }
    }
}
