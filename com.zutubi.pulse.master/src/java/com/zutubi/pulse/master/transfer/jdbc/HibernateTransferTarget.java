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

package com.zutubi.pulse.master.transfer.jdbc;

import com.zutubi.pulse.core.util.JDBCTypes;
import com.zutubi.pulse.core.util.JDBCUtils;
import com.zutubi.pulse.master.transfer.Column;
import com.zutubi.pulse.master.transfer.Table;
import com.zutubi.pulse.master.transfer.TransferException;
import com.zutubi.pulse.master.transfer.TransferTarget;
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
 * A hibernate implementation of a transfer target that writes the data to a
 * database with the help of hibernate.  It is expected that the target database
 * is blank.
 */
public class HibernateTransferTarget implements TransferTarget
{
    private static final Logger LOG = Logger.getLogger(HibernateTransferTarget.class);

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
            throw new HibernateTransferException(e);
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
                if (columnMapping.getSqlTypeCode() == null)
                {
                    // not sure why this is a problem when we run a restore via the restoration process, but not a
                    // problem when we run an import via the commands.
                    continue;
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
            throw new HibernateTransferException(e);
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
            throw new HibernateTransferException(e);
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
            throw new HibernateTransferException(e);
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
            throw new HibernateTransferException(e);
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
                throw new HibernateTransferException("Unable to create the new database schema. The table '" + table.getName() + "' " +
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
