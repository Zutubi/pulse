package com.zutubi.tove.annotations;

/**
 * The Formatter interface.
 *
 * @see com.zutubi.tove.annotations.Format
 */
public interface Formatter<T>
{
    /**
     * This method takes a object of type T, and returns a formatted string representation
     * of that object.
     *
     * @param obj is the object to be formatted.
     *
     * @return formatted string representation of the parameter.
     */
    String format(T obj);
}
