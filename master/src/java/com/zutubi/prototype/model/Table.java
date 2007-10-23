package com.zutubi.prototype.model;

import com.zutubi.prototype.AbstractParameterised;
import com.zutubi.prototype.table.TableDescriptor;

import java.util.LinkedList;
import java.util.List;
import java.util.Collections;

/**
 */
public class Table extends AbstractParameterised
{
    private List<String> headers = new LinkedList<String>();
    private List<Row> rows = new LinkedList<Row>();
    private Row firstVisible = null;
    private Row lastVisible = null;
    private int visibleRowCount = 0;
    private int width;

    public Table(int width)
    {
        this.width = width;
    }

    public int getWidth()
    {
        return width;
    }

    public boolean isOrderable()
    {
        return getParameter(TableDescriptor.PARAM_ORDER_ALLOWED, false);
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
        if(!row.isHidden())
        {
            visibleRowCount++;
            
            if(firstVisible == null)
            {
                firstVisible = row;
            }

            lastVisible = row;
        }
    }

    public boolean isAddAllowed()
    {
        return getParameter(TableDescriptor.PARAM_ADD_ALLOWED, Boolean.FALSE);
    }

    public int getVisibleRowCount()
    {
        return visibleRowCount;
    }

    public boolean isFirstVisible(Row row)
    {
        return row == firstVisible;
    }

    public boolean isLastVisible(Row row)
    {
        return row == lastVisible;
    }
}
