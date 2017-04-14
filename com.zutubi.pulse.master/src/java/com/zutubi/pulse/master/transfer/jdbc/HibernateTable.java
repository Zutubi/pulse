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
public class HibernateTable implements Table
{
    private org.hibernate.mapping.Table delegate;

    public HibernateTable(org.hibernate.mapping.Table delegate)
    {
        this.delegate = delegate;
    }

    public String getName()
    {
        return delegate.getName();
    }

    public List<Column> getColumns()
    {
        List<Column> columns = new LinkedList<Column>();

        Iterator iterator = delegate.getColumnIterator();
        while (iterator.hasNext())
        {
            columns.add(new HibernateColumn((org.hibernate.mapping.Column) iterator.next()));
        }

        return columns;
    }

    public Column getColumn(String name)
    {
        for (Column col : getColumns())
        {
            if (col.getName().equals(name))
            {
                return col;
            }
        }
        return null;
    }

    public void setName(String name)
    {
        throw new UnsupportedOperationException("hibernate tables are not mutable.");
    }
}
