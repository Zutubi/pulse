package com.zutubi.pulse.master.tove.table;

/**
 * The Formatter interface.
 *
 * @see com.zutubi.tove.annotations.Format
 */
public interface ColumnFormatter
{
    /**
     * This method takes a object of type T, and returns a formatted string representation
     * of that object.
     *
     * @param obj is the object to be formatted.
     *
     * @return formatted string representation of the parameter.
     */
    String format(Object obj);
}
