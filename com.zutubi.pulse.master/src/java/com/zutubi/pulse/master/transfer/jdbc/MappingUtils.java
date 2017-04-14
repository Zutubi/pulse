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

import com.zutubi.pulse.master.transfer.Column;
import com.zutubi.pulse.master.transfer.Table;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 *
 *
 */
public class MappingUtils
{
    public static String sqlSelectAll(org.hibernate.mapping.Table table)
    {
        return sqlSelectAll(new HibernateTable(table));
    }

    public static String sqlSelectAll(Table table)
    {
        StringBuilder builder = new StringBuilder();
        builder.append("select");
        String sep = " ";

        for (Column column : table.getColumns())
        {
            builder.append(sep).append(column.getName());
            sep = ", ";
        }
        builder.append(" from ").append(table.getName());
        return builder.toString();
    }

    public static String sqlInsert(org.hibernate.mapping.Table table)
    {
        return sqlInsert(new HibernateTable(table));
    }

    public static String sqlInsert(Table table)
    {
        StringBuilder builder = new StringBuilder();
        builder.append("insert into ").append(table.getName());
        String sep = " (";

        for (Column column : table.getColumns())
        {
            builder.append(sep).append(column.getName());
            sep = ", ";
        }
        builder.append(") values (");

        sep = "";
        for (Column column : table.getColumns())
        {
            builder.append(sep).append("?");
            sep = ", ";
        }
        builder.append(")");
        return builder.toString();
    }


    public static List<org.hibernate.mapping.Column> getColumns(org.hibernate.mapping.Table table)
    {
        List<org.hibernate.mapping.Column> columns = new LinkedList<org.hibernate.mapping.Column>();
        Iterator columnIterator = table.getColumnIterator();
        while (columnIterator.hasNext())
        {
            columns.add((org.hibernate.mapping.Column) columnIterator.next());
        }
        return columns;
    }

}
