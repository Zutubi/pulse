package com.zutubi.prototype.type.record;

/**
 * A record that supports writing to properties.
 */
public interface MutableRecord extends Record
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
     * A meta data to this record.
     *
     * @param key used to identify the meta data
     * @param value of the meta data.
     */
    void putMeta(String key, String value);

    Object put(String key, Object value);

    void clear();

    Object remove(String key);

    void update(Record record);
}
