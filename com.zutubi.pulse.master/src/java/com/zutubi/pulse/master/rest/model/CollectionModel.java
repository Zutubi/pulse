package com.zutubi.pulse.master.rest.model;

import com.zutubi.pulse.master.rest.model.tables.TableModel;

/**
 * Model representing collections.
 */
public class CollectionModel extends ConfigModel
{
    private TableModel table;

    public CollectionModel(String key, String handle, String label)
    {
        super("collection", handle, key, label);
    }

    public TableModel getTable()
    {
        return table;
    }

    public void setTable(TableModel table)
    {
        this.table = table;
    }
}
