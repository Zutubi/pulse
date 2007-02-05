package com.zutubi.prototype.form.model;

import java.util.List;
import java.util.LinkedList;

/**
 *
 *
 */
public class Row
{
    private List<Column> cells = new LinkedList<Column>();
    
    private int index;

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
        this.index = i;
    }

    public int getIndex()
    {
        return index;
    }
}
