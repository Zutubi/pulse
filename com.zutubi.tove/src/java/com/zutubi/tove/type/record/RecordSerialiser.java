package com.zutubi.tove.type.record;

/**
 * An interface for storing and loading records from permanent storage.
 *
 */
public interface RecordSerialiser
{
    /**
     * Serialise the record using this record serialiser implementation.
     *
     * @param record    the record to be serialised.
     * @param deep      indicates whether or not nested records should also be serialised.
     *
     * @throws RecordSerialiseException on error.
     */
    void serialise(Record record, boolean deep) throws RecordSerialiseException;

    /**
     * Deserialise any serialised records.
     *
     * @return  the deserialised records
     *
     * @throws RecordSerialiseException on error.
     */
    MutableRecord deserialise() throws RecordSerialiseException;
}
