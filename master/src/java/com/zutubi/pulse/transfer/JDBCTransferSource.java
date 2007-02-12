package com.zutubi.pulse.transfer;

import com.zutubi.pulse.upgrade.tasks.MutableConfiguration;
import com.zutubi.pulse.util.JDBCUtils;
import org.hibernate.engine.Mapping;
import org.hibernate.mapping.Column;
import org.hibernate.mapping.Table;
import org.hibernate.mapping.SimpleValue;

import javax.sql.DataSource;
import java.sql.*;
import java.util.*;

/**
 *
 *
 */
public class JDBCTransferSource implements TransferSource
{
    private DataSource dataSource;

    private MutableConfiguration configuration;

    public void setDataSource(DataSource dataSource)
    {
        this.dataSource = dataSource;
    }

    public void setConfiguration(MutableConfiguration configuration)
    {
        this.configuration = configuration;
    }

    public void transferTo(TransferTarget target) throws TransferException
    {
        try
        {
            configuration.buildMappings();
            Mapping mapping = configuration.getMapping();

            target.start();

            Connection con = null;
            try
            {
                con = dataSource.getConnection();

                Iterator tables = configuration.getTableMappings();
                while (tables.hasNext())
                {
                    Table table = (Table) tables.next();
                    List<Column> columns = MappingUtils.getColumns(table);
                    for (Column column : columns)
                    {
                        if (column.getSqlTypeCode() == null)
                        {
                            column.setSqlTypeCode(column.getSqlTypeCode(mapping));
                        }
                    }

                    exportTable(target, table, con, columns);
                }

                // handle the special case.
                Table table = HibernateUniqueKeyTable.getMapping();
                exportTable(target, table, con, MappingUtils.getColumns(table));

            }
            finally
            {
                JDBCUtils.close(con);
            }

            target.end();
        }
        catch (SQLException e)
        {
            throw new TransferException(e);
        }
    }

    private void exportTable(TransferTarget target, Table table, Connection con, List<Column> columns)
            throws TransferException, SQLException
    {
        target.startTable(table);

        String sql = MappingUtils.sqlSelectAll(table);

        PreparedStatement statement = null;
        ResultSet rows = null;
        try
        {
            statement = con.prepareStatement(sql);

            rows = statement.executeQuery();
            while (rows.next())
            {
                Map<String, Object> row = new HashMap<String, Object>();
                for (Column column : columns)
                {
                    row.put(column.getName(), rows.getObject(column.getName()));
                }
                target.row(row);
            }
        }
        finally
        {
            JDBCUtils.close(rows);
            JDBCUtils.close(statement);
        }

        target.endTable();
    }
}
