package com.zutubi.util;

/**
 * A simple grid cell, which holds arbitrary data.
 */
public class GridCell<T>
{
    private T data;

    /**
     * Retrieves the data from the cell.
     *
     * @return this cell's data, may be null
     */
    public T getData()
    {
        return data;
    }

    /**
     * Stores the data in this cell.
     *
     * @param data data to store
     */
    public void setData(T data)
    {
        this.data = data;
    }
}
