package com.zutubi.pulse.master.transfer;

/**
 *
 *
 */
public interface Column
{
    /**
     * Get the name of the column
     * @return name identifying the column.
     */
    String getName();

    /**
     * Get the sql type code of the datatype for this column
     *
     * @return sql code
     */
    int getSqlTypeCode();
}
