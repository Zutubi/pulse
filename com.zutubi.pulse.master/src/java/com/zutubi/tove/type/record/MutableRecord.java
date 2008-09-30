package com.zutubi.tove.type.record;

/**
 * A record that supports writing to properties.
 */
public interface MutableRecord extends Record, Cloneable
{
    /**
     * Set the symbolic name for the data contained by this record.  The symbolic name defines the type of the
     * records data.
     *
     * @param name symbolic name
     *
     * @see com.zutubi.tove.type.TypeRegistry
     */
    void setSymbolicName(String name);

    /**
     * A meta data to this record.
     *
     * @param key used to identify the meta data
     * @param value of the meta data.
     */
    void putMeta(String key, String value);

    String removeMeta(String key);

    Object put(String key, Object value);

    Object remove(String key);

    void clear();

    void update(Record record);

    void setHandle(long handle);

    void setPermanent(boolean permanenet);
}
