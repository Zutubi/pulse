package com.zutubi.pulse.master.rest.model.tables;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a table: a way to display a collection in the UI.
 */
public class TableModel
{
    private String heading;
    private List<ColumnModel> columns = new ArrayList<>();

    public TableModel(String heading)
    {
        this.heading = heading;
    }

    public String getHeading()
    {
        return heading;
    }

    public List<ColumnModel> getColumns()
    {
        return columns;
    }

    public void addColumn(ColumnModel column)
    {
        columns.add(column);
    }
}
