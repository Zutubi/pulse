package com.zutubi.tove.type.record;

import com.zutubi.util.GraphFunction;

import java.util.Collection;
import java.util.Set;

/**
 * A record defines a simple map for storing data.  
 */
public interface Record
{
    /**
     * Get the symbolic name associated with the data for this record.  The
     * symbolic name defines the type of the record's data.  For collections,
     * it is null.  Otherwise, it is the symbolic name of a type registered
     * with the type registry.
     * 
     * @return the symbolic name
     *
     * @see com.zutubi.tove.type.TypeRegistry
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
     * Test whether or not the specified meta key is present within the record.
     *
     * @param key being tested
     *
     * @return true if the meta key is present in the record, false otherwise.
     */
    boolean containsMetaKey(String key);

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
     * @param deep            if true, the copy is deep: child records are also
     *                        copied, if false child records will still be
     *                        present in the copy, but will be reused from the
     *                        original
     * @param preserveHandles if true the copy will retain the handles in this
     *                        record (suitable for saving in-place changes), if
     *                        false the copy will have no handles (suitable for
     *                        inserting a new record based on this one)
     * @return a value copy of this record
     */
    MutableRecord copy(boolean deep, boolean preserveHandles);

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

    /**
     * @return true if this record can never be deleted, i.e. it is permanent
     */
    boolean isPermanent();

    /**
     * @return true if this record is a collection (as opposed to a
     *         composite)
     */
    boolean isCollection();

    /**
     * Returns true if the meta and simple keys and values in the given
     * record are the same as in this record.
     *
     * @param other record to test equality to
     * @return true iff the other record's simple values are the same as this
     *         record
     */
    boolean shallowEquals(Record other);

    boolean metaEquals(Record other);
    boolean simpleEquals(Record other);

    /**
     * Executes the given function over this and all nested records
     * recursively.
     *
     * @param f function to execute
     */
    void forEach(GraphFunction<Record> f);
}
