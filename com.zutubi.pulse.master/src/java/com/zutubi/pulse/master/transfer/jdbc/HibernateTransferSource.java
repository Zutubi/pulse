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

import com.zutubi.pulse.core.util.JDBCUtils;
import com.zutubi.pulse.master.transfer.TransferException;
import com.zutubi.pulse.master.transfer.TransferSource;
import com.zutubi.pulse.master.transfer.TransferTarget;
import org.hibernate.cfg.Configuration;
import org.hibernate.engine.spi.Mapping;
import org.hibernate.mapping.Column;
import org.hibernate.mapping.Table;

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
