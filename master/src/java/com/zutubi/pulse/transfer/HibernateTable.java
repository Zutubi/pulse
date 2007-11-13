package com.zutubi.pulse.transfer;

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
