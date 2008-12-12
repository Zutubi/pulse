package com.zutubi.tove.config.api;

/**
 * Defines an interface that can format objects of type T into human-readable
 * string representations.
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
