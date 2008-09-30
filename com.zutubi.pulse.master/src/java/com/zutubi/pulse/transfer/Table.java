package com.zutubi.pulse.transfer;

import java.util.List;

/**
 *
 *
 */
public interface Table
{
    /**
     * The name of the table being transfered.
     *
     * @return name of the table.
     */
    String getName();

    /**
     * Get the list of columns associated with this table.
     *
     * @return a list of columns.
     */
    List<Column> getColumns();

    /**
     * Get the named column associated with this table.
     * @param name of the column being requested.
     * @return the column instance, or null if that column does not exist.
     */
    Column getColumn(String name);
}
