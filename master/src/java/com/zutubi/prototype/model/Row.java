package com.zutubi.prototype.model;

import com.zutubi.prototype.type.record.PathUtils;

import java.util.LinkedList;
import java.util.List;

/**
 */
public class Row
{
    private String path = "";
    private List<Cell> cells = new LinkedList<Cell>();
    private List<RowAction> actions = null;

    public Row()
    {
    }

    public Row(String path, List<RowAction> actions)
    {
        this.path = path;
        this.actions = actions;
    }

    public String getPath()
    {
        return path;
    }

    public String getBaseName()
    {
        return PathUtils.getBaseName(path);
    }

    public List<Cell> getCells()
    {
        return cells;
    }

    public void addCell(Cell cell)
    {
        cells.add(cell);
    }

    public List<RowAction> getActions()
    {
        return actions;
    }

    public void addAction(RowAction action)
    {
        actions.add(action);
    }
}
