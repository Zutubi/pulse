package com.zutubi.prototype.type.record;

import java.util.Map;
import java.util.Set;
import java.util.Collection;

/**
 * A record defines a simple map for storing data.  
 *
 */
public interface Record extends Cloneable
{
    /**
     * Set the symbolic name for the data contained by this record.  The symbolic name defines the type of the
     * records data.
     *
     * @param name symbolic name
     *
     * @see com.zutubi.prototype.type.TypeRegistry
     */
    void setSymbolicName(String name);

    /**
     * Get the symbolic name associated with the data for this record.  The symbolic name defined the type of the
     * records data.
     * 
     * @return the symbolic name
     *
     * @see com.zutubi.prototype.type.TypeRegistry
     */
    String getSymbolicName();

    /**
     * A meta data to this record.
     *
     * @param key used to identify the meta data
     * @param value of the meta data.
     */
    void putMeta(String key, String value);

    /**
     * Retrieve meta data associated with this record.
     *
     * @param key of the meta data to be retrieved.
     *
     * @return value of the meta data, or null if the meta data does not exist.
     */
    String getMeta(String key);

    Object get(Object key);

    int size();

    Object put(String key, Object value);

    void putAll(Record other);

    void clear();

    Object remove(Object key);

    boolean containsKey(Object key);

    /**
     * Create a clone of this record.
     *
     * @return a deep value copy of this record.
     *
     * @throws CloneNotSupportedException
     */
    Record clone() throws CloneNotSupportedException;

    Set<String> keySet();
}
