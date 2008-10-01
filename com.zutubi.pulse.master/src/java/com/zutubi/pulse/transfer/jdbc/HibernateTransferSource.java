package com.zutubi.pulse.transfer.jdbc;

import com.zutubi.pulse.core.util.JDBCUtils;
import com.zutubi.pulse.transfer.TransferSource;
import com.zutubi.pulse.transfer.TransferTarget;
import com.zutubi.pulse.transfer.TransferException;
import com.zutubi.pulse.transfer.jdbc.HibernateMapping;
import com.zutubi.pulse.transfer.jdbc.MappingUtils;
import com.zutubi.pulse.transfer.jdbc.HibernateUniqueKeyTable;
import com.zutubi.pulse.transfer.jdbc.HibernateTable;
import org.hibernate.engine.Mapping;
import org.hibernate.mapping.Column;
import org.hibernate.mapping.Table;
import org.hibernate.cfg.Configuration;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 *
 *
 */
public class HibernateTransferSource implements TransferSource
{
    private DataSource dataSource;

    private Configuration configuration;

    public void setDataSource(DataSource dataSource)
    {
        this.dataSource = dataSource;
    }

    public void setConfiguration(Configuration configuration)
    {
        this.configuration = configuration;
    }

    public void transferTo(TransferTarget target) throws TransferException
    {
        try
        {
            // ensure that the mappings are built.
            configuration.buildMappings();
            Mapping mapping = new HibernateMapping(configuration);

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
            throw new HibernateTransferException(e);
        }
    }

    private void exportTable(TransferTarget target, Table table, Connection con, List<Column> columns) throws TransferException, SQLException
    {
        target.startTable(new HibernateTable(table));

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
