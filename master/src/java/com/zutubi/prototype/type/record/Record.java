package com.zutubi.prototype.type.record;

import java.util.Collection;
import java.util.Set;

/**
 * A record defines a simple map for storing data.  
 *
 */
public interface Record
{
    /**
     * Get the symbolic name associated with the data for this record.  The
     * symbolic name defines the type of the record's data.  For collections,
     * it is one of the CollectionType.SN_* constants.  Otherwise, it is the
     * symbolic name of a type registered with the type registry.
     * 
     * @return the symbolic name
     *
     * @see com.zutubi.prototype.type.TypeRegistry
     */
    String getSymbolicName();

    /**
     * Retrieve meta data associated with this record.
     *
     * @param key of the meta data to be retrieved.
     *
     * @return value of the meta data, or null if the meta data does not exist.
     */
    String getMeta(String key);

    /**
     * Retrieve the value associated with the specified key.
     *
     * @param key that identifies the data being retrieved.
     *
     * @return the identified value, or null if the key is not present.
     */
    Object get(String key);

    /**
     * The size of the record is defined by the number of entries it contains.  An entry is considered
     * to be any key:value pair, regarless of whether the key references another record or a simple data
     * value.
     *
     * @return the size of this record.
     */
    int size();

    /**
     * Test whether or not the specified key is present within the record.
     *
     * @param key being tested
     *
     * @return true if the key is present in the record, false otherwise.
     */
    boolean containsKey(String key);

    /**
     * Create a copy of this record.
     *
     * @param deep if true, the copy is deep: child records are also copied
     * 
     * @return a value copy of this record
     */
    MutableRecord copy(boolean deep);

    /**
     * Retrieve the set of keys to the data contained within this record.
     *
     * @return a set of key values
     */
    Set<String> keySet();

    /**
     * Retrieve the set of keys to the meta data contained within this record.
     *
     * @return a set of key values
     */
    Set<String> metaKeySet();

    Set<String> simpleKeySet();

    Set<String> nestedKeySet();

    Collection<Object> values();

    /**
     * @return the unique handle for this record, or 0 if this record has never
     *         been saved.
     */
    long getHandle();

    boolean isPermanent();

    boolean isCollection();
}
