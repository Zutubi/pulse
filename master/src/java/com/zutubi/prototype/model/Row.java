package com.zutubi.prototype.model;

import java.util.List;
import java.util.LinkedList;

/**
 *
 *
 */
public class Row extends UIComponent
{
    private List<Column> cells = new LinkedList<Column>();
    
    public void addCell(Column column)
    {
        cells.add(column);
    }

    public List<Column> getCells()
    {
        return cells;
    }

    public void setIndex(int i)
    {
        parameters.put("index", i);
    }

    public int getIndex()
    {
        return (Integer)parameters.get("index");
    }
}
