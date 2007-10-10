package com.zutubi.prototype.model;

import com.zutubi.prototype.AbstractParameterised;
import com.zutubi.prototype.table.TableDescriptor;

import java.util.LinkedList;
import java.util.List;

/**
 */
public class Table extends AbstractParameterised
{
    private List<String> headers = new LinkedList<String>();
    private List<Row> rows = new LinkedList<Row>();
    private int width;

    public Table(int width)
    {
        this.width = width;
    }

    public int getWidth()
    {
        return width;
    }

    public String getHeading()
    {
        return (String) getParameter(TableDescriptor.PARAM_HEADING);
    }

    public List<String> getHeaders()
    {
        return headers;
    }

    public void addHeader(String key)
    {
        headers.add(key);
    }

    public List<Row> getRows()
    {
        return rows;
    }

    public void addRow(Row row)
    {
        rows.add(row);
    }

    public boolean isAddAllowed()
    {
        return getParameter(TableDescriptor.PARAM_ADD_ALLOWED, Boolean.FALSE);
    }
}
